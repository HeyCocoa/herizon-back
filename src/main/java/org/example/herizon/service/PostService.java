package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.CreatePostRequest;
import org.example.herizon.dto.PollOptionDTO;
import org.example.herizon.dto.PostDTO;
import org.example.herizon.dto.TagDTO;
import org.example.herizon.entity.*;
import org.example.herizon.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 帖子业务逻辑服务类
 * <p>
 * 提供帖子相关的核心业务功能：
 * - 分页查询帖子列表（支持标签筛选和排序）
 * - 查询帖子详情（含用户操作状态）
 * - 创建新帖子（含标签处理）
 * - 帖子数据转换和辅助方法
 *
 * @author Kokoa
 */
@Service
public class PostService{

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private PollOptionMapper pollOptionMapper;

    @Autowired
    private UserVoteMapper userVoteMapper;

    @Autowired
    private FollowService followService;

    /**
     * 搜索帖子（模糊匹配）
     * <p>
     * 根据关键词搜索帖子，支持：
     * 1. 对标题和内容进行模糊匹配（LIKE查询）
     * 2. 按创建时间倒序排列（最新的在前）
     * 3. 仅返回正常状态的帖子
     * <p>
     * 技术实现：
     * - 使用 LIKE '%keyword%' 进行模糊匹配
     * - 在title或content中匹配即返回结果
     * - ORDER BY created_at DESC 确保最新帖子优先
     *
     * @param keyword 搜索关键词（会自动去除首尾空格）
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，包含匹配的帖子列表
     */
    public PageResult<PostDTO> searchPosts(String keyword, Integer current, Integer size) {
        // 关键词预处理：去除首尾空格
        String cleanKeyword = keyword != null ? keyword.trim() : "";

        // 如果关键词为空，返回空结果
        if (cleanKeyword.isEmpty()) {
            return PageResult.of(List.of(), 0L, (long) current, (long) size);
        }

        // 构建模糊搜索查询
        // 技术要点：
        // 1. 使用 LEFT JOIN 获取用户信息（昵称、头像、角色）
        // 2. WHERE条件：status=0（正常帖子）AND (title LIKE '%keyword%' OR content LIKE '%keyword%')
        // 3. ORDER BY created_at DESC（最新发布优先）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username, users.nickname, users.avatar, users.role")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.status = 0") // 仅查询正常状态的帖子
                .and("(posts.title LIKE ? OR posts.content LIKE ?)",
                     "%" + cleanKeyword + "%", "%" + cleanKeyword + "%") // 模糊匹配标题或内容
                .orderBy("posts.created_at DESC"); // 按时间降序（最新的在前）

        // 执行分页查询
        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO（包含标签、用户操作状态等完整信息）
        List<PostDTO> postDTOs = convertToDTOList(page.getRecords());

        // 返回分页结果
        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 首页帖子列表（系统变更后的简化版）
     * <p>
     * 根据系统变更要求：
     * 1. 移除所有筛选条件，仅展示全部帖子
     * 2. 实现简单的推荐算法排序
     * 3. 不再支持复杂的排序选项和标签筛选
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，按推荐算法排序的帖子列表
     */
    public PageResult<PostDTO> getHomePostList(Integer current, Integer size){
        // 创建查询包装器，仅查询正常状态的帖子
        // 选择帖子所有字段以及用户的nickname、avatar、role（用于前端显示）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username, users.nickname, users.avatar, users.role")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.status = 0");  // 只查询正常状态的帖子

        // 实现简化的推荐算法排序（修复MyBatis-Flex SQL安全检查问题）
        // 为避免SQL安全检查问题，使用多级排序而非算术表达式
        // 1. 优先按点赞数排序（主要热度指标）
        // 2. 其次按收藏数排序（用户价值指标）
        // 3. 再按分享数排序（传播价值指标）
        // 4. 最后按时间排序（最新发布优先）
        queryWrapper.orderBy("posts.like_count DESC")
                .orderBy("posts.collect_count DESC")
                .orderBy("posts.share_count DESC")
                .orderBy("posts.created_at DESC");

        // 执行分页查询
        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO
        List<PostDTO> postDTOs = convertToDTOList(page.getRecords());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 根据标签查询帖子列表（话题页面专用）
     * <p>
     * 为新的话题页面提供API支持，显示指定标签下的所有帖子
     *
     * @param tagId   标签ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，该标签下的帖子列表
     */
    public PageResult<PostDTO> getPostsByTag(Long tagId, Integer current, Integer size){
        // 创建查询包装器，筛选指定标签的帖子
        // 选择帖子所有字段以及用户的nickname、avatar、role（用于前端显示）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username, users.nickname, users.avatar, users.role")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.status = 0")
                // 使用EXISTS子查询筛选标签
                .and("EXISTS (SELECT 1 FROM post_tags WHERE post_tags.post_id = posts.id AND post_tags.tag_id = ? AND post_tags.deleted = 0)", tagId)
                // 按时间降序排序显示最新内容
                .orderBy("posts.created_at DESC");

        // 执行分页查询
        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO
        List<PostDTO> postDTOs = convertToDTOList(page.getRecords());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 统计指定标签下的帖子数量
     * <p>
     * 实时查询指定标签下的有效帖子总数
     * 用于话题页面右侧实时显示帖子数量，不依赖缓存字段
     *
     * @param tagId 标签ID
     * @return 该标签下的帖子总数
     */
    public Long getPostCountByTag(Long tagId) {
        // 创建查询包装器，统计指定标签的帖子数量
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("COUNT(*)")
                .from("posts")
                .where("posts.status = 0")
                .and("posts.deleted = 0")
                // 使用EXISTS子查询筛选标签
                .and("EXISTS (SELECT 1 FROM post_tags WHERE post_tags.post_id = posts.id AND post_tags.tag_id = ? AND post_tags.deleted = 0)", tagId);

        // 执行统计查询
        Object countObj = postMapper.selectObjectByQuery(queryWrapper);
        return countObj != null ? ((Number) countObj).longValue() : 0L;
    }

    /**
     * 根据ID查询帖子详情
     *
     * @param id            帖子ID
     * @param currentUserId 当前用户ID，可为null。如果提供，会查询用户对该帖子的操作状态
     * @return 帖子详情DTO，如果帖子不存在返回null
     */
    public PostDTO getPostById(Long id, Long currentUserId){
        Post post = postMapper.selectOneById(id);
        if( post==null ){
            return null;
        }

        // 自动累加浏览量（处理空值避免异常）
        int currentViewCount = post.getViewCount() != null ? post.getViewCount() : 0;
        post.setViewCount(currentViewCount + 1);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.update(post);

        PostDTO postDTO = convertToDTO(post);

        // 如果提供了当前用户ID，查询用户对该帖子的操作状态
        if( currentUserId!=null ){
            postDTO.setIsLiked(hasUserAction(currentUserId, id, "post", 0));
            postDTO.setIsCollected(hasUserAction(currentUserId, id, "post", 1));
            if( currentUserId!=null && !currentUserId.equals(post.getUserId()) ) {
                postDTO.setIsAuthorFollowed(followService.isFollowing(currentUserId, post.getUserId()));
            }

            // 如果是投票帖，查询用户的投票选择
            if( post.getPostType()!=null && post.getPostType()==1 ){
                postDTO.setMyVote(getMyVote(id, currentUserId));
            }
        }

        return postDTO;
    }

    /**
     * 创建新帖子
     * <p>
     * 修订历史（2025-10-01）：
     * 1. 修复字段名匹配：getPostType() → getType()
     * 2. 修复标签处理：getTags() → getTagIds()，使用标签ID而非名称
     * 3. 新增图片列表支持：设置imageUrls字段
     * 4. 新增投票功能支持：验证pollOptions并预留处理逻辑
     * 5. 统一使用POST /api/posts创建所有类型的帖子（包括投票帖）
     *
     * @param request 创建帖子请求，包含标题、内容、类型、标签、图片、投票选项等
     * @param userId  创建者用户ID
     * @return 创建的帖子实体
     */
    @Transactional
    public Post createPost(CreatePostRequest request, Long userId){
        // 安全检查：防止必填字段为null导致数据库错误
        if( request.getTitle()==null || request.getTitle().trim().isEmpty() ){
            throw new RuntimeException("帖子标题不能为空");
        }
        if( request.getContent()==null || request.getContent().trim().isEmpty() ){
            throw new RuntimeException("帖子内容不能为空");
        }

        // 如果是投票帖，验证投票选项
        // 修复：使用getType()而非getPostType()，匹配前端字段名
        if( request.getType()!=null && request.getType()==1 ){
            if( request.getPollOptions()==null || request.getPollOptions().size() < 2 ){
                throw new RuntimeException("投票帖至少需要2个选项");
            }
            if( request.getPollOptions().size() > 5 ){
                throw new RuntimeException("投票帖最多支持5个选项");
            }
        }

        // 创建帖子实体
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // 修复：使用getType()而非getPostType()
        post.setPostType(request.getType()!=null ? request.getType() : 0);

        // 新增：设置图片URL列表（支持最多3张图片）
        post.setImageUrls(request.getImageUrls());

        // 初始化统计字段
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setShareCount(0);
        post.setCollectCount(0);
        post.setCommentCount(0);  // 添加评论数初始化
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setStatus(0);
        post.setDeleted(0);  // 添加逻辑删除标记初始化

        postMapper.insert(post);

        // 处理投票选项（2025-10-01已实现）
        // 如果是投票帖且提供了投票选项，批量创建投票选项记录
        if( request.getType()!=null && request.getType()==1 && request.getPollOptions()!=null ){
            // 验证已在createPost方法开头完成（2-5个选项）
            // 遍历投票选项列表，按顺序创建记录
            for( int i = 0; i < request.getPollOptions().size(); i++ ){
                PollOption option = new PollOption();
                option.setPostId(post.getId());                      // 关联帖子ID
                option.setOptionText(request.getPollOptions().get(i)); // 选项文本
                option.setDisplayOrder(i + 1);                       // 显示顺序（从1开始）
                option.setCreatedAt(LocalDateTime.now());            // 创建时间
                option.setDeleted(0);                                 // 逻辑删除标记（0=正常）
                pollOptionMapper.insert(option);                     // 插入数据库
            }
        }

        // 处理帖子标签：修复为使用标签ID而非标签名称
        // 修改：getTags() → getTagIds()，使用Long类型的标签ID列表
        if( request.getTagIds()!=null && !request.getTagIds().isEmpty() ){
            for( Long tagId : request.getTagIds() ){
                // 验证标签是否存在（改为通过ID查询）
                Tag tag = tagMapper.selectOneById(tagId);
                if( tag==null || tag.getDeleted()==1 ){
                    throw new RuntimeException("标签不存在或已删除: ID=" + tagId);
                }

                // 创建帖子-标签关联记录
                PostTag postTag = new PostTag();
                postTag.setPostId(post.getId());
                postTag.setTagId(tagId);  // 直接使用传入的标签ID
                postTag.setDeleted(0);     // 设置未删除状态
                postTagMapper.insert(postTag);

                // 更新标签的帖子数量计数器
                // 直接增加post_count字段，确保话题卡片显示正确的帖子数量
                tag.setPostCount((tag.getPostCount() == null ? 0 : tag.getPostCount()) + 1);
                tagMapper.update(tag);
            }
        }

        return post;
    }

    /**
     * 获取已存在的标签
     * <p>
     * 根据系统变更要求，只有管理员可以创建新标签
     * 用户发帖时只能选择已有标签，不再自动创建新标签
     *
     * @param tagName 标签名称
     * @return 标签实体，如果标签不存在返回null
     * @throws RuntimeException 当标签不存在时抛出异常
     */
    private Tag getExistingTag(String tagName){
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("name = ?", tagName)
                .and("deleted = 0");  // 只查询未删除的标签

        Tag tag = tagMapper.selectOneByQuery(queryWrapper);

        if( tag==null ){
            throw new RuntimeException("标签 '" + tagName + "' 不存在，请选择已有标签或联系管理员创建");
        }

        return tag;
    }

    /**
     * 将帖子实体转换为DTO
     * <p>
     * 转换过程中会填充用户信息（username、nickname、avatar、role）
     * 以及帖子的标签列表、图片URL列表、视频URL等扩展字段
     *
     * @param post 帖子实体
     * @return 帖子DTO，包含完整的用户信息和标签信息
     */
    private PostDTO convertToDTO(Post post){
        if (post == null) {
            return null;
        }
        List<PostDTO> dtos = convertToDTOList(List.of(post));
        return dtos.isEmpty() ? null : dtos.get(0);
    }

    private List<PostDTO> convertToDTOList(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<Post> validPosts = posts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (validPosts.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = validPosts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = loadUsers(userIds);

        List<Long> postIds = validPosts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, List<TagDTO>> tagMap = loadTagsByPostIds(postIds);
        Map<Long, List<PollOptionDTO>> pollOptionMap = loadPollOptionsByPostIds(validPosts);

        return validPosts.stream()
                .map(post -> buildPostDTO(post, userMap, tagMap, pollOptionMap))
                .collect(Collectors.toList());
    }

    private PostDTO buildPostDTO(Post post,
                                 Map<Long, User> userMap,
                                 Map<Long, List<TagDTO>> tagMap,
                                 Map<Long, List<PollOptionDTO>> pollOptionMap) {

        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);

        User user = userMap.get(post.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setUserAvatar(user.getAvatar());
            dto.setUserRole(user.getRole());
        }

        dto.setTags(tagMap.getOrDefault(post.getId(), List.of()));
        dto.setImageUrls(post.getImageUrls() != null ? post.getImageUrls() : List.of());
        dto.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0);

        if (post.getPostType() != null && post.getPostType() == 1) {
            dto.setPollOptions(pollOptionMap.getOrDefault(post.getId(), List.of()));
        }

        dto.setIsAuthorFollowed(Boolean.FALSE);
        return dto;
    }

    private Map<Long, User> loadUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(User::getId).in(userIds);
        List<User> users = userMapper.selectListByQuery(queryWrapper);

        Map<Long, User> result = new HashMap<>();
        for (User user : users) {
            if (user != null && user.getId() != null) {
                result.put(user.getId(), user);
            }
        }
        return result;
    }

    private Map<Long, List<TagDTO>> loadTagsByPostIds(Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper relationQuery = QueryWrapper.create()
                .where(PostTag::getPostId).in(postIds)
                .and(PostTag::getDeleted).eq(0);
        List<PostTag> relations = postTagMapper.selectListByQuery(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = relations.stream()
                .map(PostTag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper tagQuery = QueryWrapper.create()
                .where(Tag::getId).in(tagIds)
                .and(Tag::getDeleted).eq(0);
        List<Tag> tags = tagMapper.selectListByQuery(tagQuery);

        Map<Long, Tag> tagMap = new HashMap<>();
        for (Tag tag : tags) {
            if (tag != null && tag.getId() != null) {
                tagMap.put(tag.getId(), tag);
            }
        }

        Map<Long, List<TagDTO>> result = new HashMap<>();
        for (PostTag relation : relations) {
            if (relation == null || relation.getPostId() == null) {
                continue;
            }
            Tag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            TagDTO dto = new TagDTO();
            dto.setId(tag.getId());
            dto.setName(tag.getName());
            dto.setDescription(tag.getDescription());
            dto.setPostCount(tag.getPostCount());
            dto.setCreatedAt(tag.getCreatedAt());
            result.computeIfAbsent(relation.getPostId(), key -> new ArrayList<>()).add(dto);
        }
        return result;
    }

    private Map<Long, List<PollOptionDTO>> loadPollOptionsByPostIds(Collection<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> pollPostIds = posts.stream()
                .filter(post -> post != null && Objects.equals(post.getPostType(), 1))
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (pollPostIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper optionsQuery = QueryWrapper.create()
                .where(PollOption::getPostId).in(pollPostIds)
                .and(PollOption::getDeleted).eq(0)
                .orderBy("post_id ASC")
                .orderBy("display_order ASC");
        List<PollOption> options = pollOptionMapper.selectListByQuery(optionsQuery);
        if (options == null || options.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> optionIds = options.stream()
                .map(PollOption::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Integer> voteCountMap = new HashMap<>();
        if (!optionIds.isEmpty()) {
            QueryWrapper voteCountQuery = QueryWrapper.create()
                    .select("option_id AS optionId")
                    .select("COUNT(*) AS voteCount")
                    .from("user_votes")
                    .where(UserVote::getOptionId).in(optionIds)
                    .and(UserVote::getDeleted).eq(0)
                    .groupBy("option_id");
            List<Row> rows = userVoteMapper.selectRowsByQuery(voteCountQuery);
            for (Row row : rows) {
                if (row == null) {
                    continue;
                }
                Long optionId = toLong(resolveColumnValue(row, "optionId"));
                Long count = toLong(resolveColumnValue(row, "voteCount"));
                if (optionId != null) {
                    voteCountMap.put(optionId, count != null ? count.intValue() : 0);
                }
            }
        }

        Map<Long, List<PollOptionDTO>> result = new HashMap<>();
        for (PollOption option : options) {
            if (option == null || option.getPostId() == null || option.getId() == null) {
                continue;
            }
            PollOptionDTO dto = new PollOptionDTO();
            dto.setId(option.getId());
            dto.setOptionText(option.getOptionText());
            dto.setDisplayOrder(option.getDisplayOrder());
            dto.setVoteCount(voteCountMap.getOrDefault(option.getId(), 0));
            result.computeIfAbsent(option.getPostId(), key -> new ArrayList<>()).add(dto);
        }
        return result;
    }

    private Object resolveColumnValue(Row row, String alias) {
        if (row == null || alias == null) {
            return null;
        }
        Object value = row.get(alias);
        if (value != null) {
            return value;
        }
        String upper = alias.toUpperCase(Locale.ROOT);
        value = row.get(upper);
        if (value != null) {
            return value;
        }
        return row.get(alias.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ROOT));
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查用户是否对目标对象执行了特定操作
     *
     * @param userId     用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     * @param actionType 操作类型：0=点赞，1=收藏，2=分享，3=举报
     * @return true=已执行该操作，false=未执行
     */
    private boolean hasUserAction(Long userId, Long targetId, String targetType, Integer actionType){
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("target_id = ?", targetId)
                .and("target_type = ?", targetType)
                .and("action_type = ?", actionType)
                .and("deleted = 0");

        return userActionMapper.selectCountByQuery(queryWrapper) > 0;
    }

    /**
     * 对投票帖进行投票
     * <p>
     * 业务规则：
     * 1. 验证帖子是否存在且为投票帖（post_type=1）
     * 2. 验证投票选项是否属于该帖子
     * 3. 检查用户是否已投票，如已投票则先撤回原投票
     * 4. 创建新的投票记录
     * <p>
     * 并发控制：通过数据库唯一约束uk_user_post保证每人每帖只有一个有效投票
     *
     * @param postId   投票帖ID
     * @param optionId 所选选项ID
     * @param userId   投票用户ID
     * @throws RuntimeException 如果帖子不存在、不是投票帖、选项不属于该帖子等
     */
    @Transactional
    public void vote(Long postId, Long optionId, Long userId){
        // 1. 验证帖子是否存在且为投票帖
        Post post = postMapper.selectOneById(postId);
        if( post==null || post.getDeleted()==1 ){
            throw new RuntimeException("帖子不存在");
        }
        if( post.getPostType()!=1 ){
            throw new RuntimeException("该帖子不是投票帖，无法投票");
        }

        // 2. 验证投票选项是否存在且属于该帖子
        PollOption option = pollOptionMapper.selectOneById(optionId);
        if( option==null || option.getDeleted()==1 ){
            throw new RuntimeException("投票选项不存在");
        }
        if( !option.getPostId().equals(postId) ){
            throw new RuntimeException("投票选项不属于该帖子");
        }

        // 3. 检查用户是否已投票
        QueryWrapper voteQuery = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("post_id = ?", postId)
                .and("deleted = 0");
        UserVote existingVote = userVoteMapper.selectOneByQuery(voteQuery);

        // 4. 如果已投票且选择相同选项，则提示已投票
        if( existingVote!=null && existingVote.getOptionId().equals(optionId) ){
            throw new RuntimeException("您已对该选项投过票");
        }

        // 5. 如果已投票但选择不同选项，则撤回原投票（逻辑删除）
        if( existingVote!=null ){
            existingVote.setDeleted(1);
            userVoteMapper.update(existingVote);
        }

        // 6. 创建新的投票记录
        UserVote newVote = new UserVote();
        newVote.setUserId(userId);
        newVote.setPostId(postId);
        newVote.setOptionId(optionId);
        newVote.setCreatedAt(LocalDateTime.now());
        newVote.setDeleted(0);
        userVoteMapper.insert(newVote);
    }

    /**
     * 查询用户在某个投票帖中的投票选择
     * <p>
     * 用于前端显示用户已选择的投票选项
     *
     * @param postId 投票帖ID
     * @param userId 用户ID
     * @return 用户选择的选项ID，如果未投票返回null
     */
    public Long getMyVote(Long postId, Long userId){
        // 查询用户的有效投票记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("post_id = ?", postId)
                .and("deleted = 0");

        UserVote vote = userVoteMapper.selectOneByQuery(queryWrapper);
        return vote!=null ? vote.getOptionId() : null;
    }

    /**
     * 查询指定用户发布的帖子列表
     * <p>
     * 用于"我的帖子"页面，返回用户发布的所有帖子
     * 支持分页查询，按创建时间降序排列
     *
     * @param userId  用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页的帖子DTO列表
     */
    public PageResult<PostDTO> getUserPosts(Long userId, Integer current, Integer size) {
        // 创建查询包装器，筛选指定用户的帖子
        // 查询帖子所有字段以及用户信息
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username, users.nickname, users.avatar, users.role")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.user_id = ?", userId)
                .and("posts.deleted = 0")
                .orderBy("posts.created_at DESC");  // 按创建时间降序排列

        // 执行分页查询
        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO
        List<PostDTO> postDTOs = convertToDTOList(page.getRecords());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 管理员分页查询全部帖子
     * <p>
     * 查询所有未删除的帖子，按创建时间倒序排列，用于后台管理页面。
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页的帖子DTO列表
     */
    public PageResult<PostDTO> getAllPostsForAdmin(Integer current, Integer size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username, users.nickname, users.avatar, users.role")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.deleted = 0")
                .orderBy("posts.created_at DESC");

        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);
        List<PostDTO> postDTOs = convertToDTOList(page.getRecords());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 删除帖子（仅作者或管理员）
     * <p>
     * 使用逻辑删除保护数据一致性，同时清理关联的标签、投票选项以及用户行为记录。
     *
     * @param postId 帖子ID
     * @param userId 操作用户ID
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        if (postId == null) {
            throw new RuntimeException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new RuntimeException("用户信息不存在");
        }

        Post post = postMapper.selectOneById(postId);
        if (post == null || Objects.equals(post.getDeleted(), 1)) {
            throw new RuntimeException("帖子不存在或已被删除");
        }

        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean isOwner = Objects.equals(post.getUserId(), userId);
        boolean isAdmin = user.getRole() != null && user.getRole() == 2;
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("无权删除该帖子");
        }

        post.setDeleted(1);
        post.setStatus(1);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.update(post);

        // 同步逻辑删除帖子与标签关联
        QueryWrapper tagQuery = QueryWrapper.create()
                .where("post_id = ?", postId)
                .and("(deleted = 0 OR deleted IS NULL)");
        List<PostTag> tagRelations = postTagMapper.selectListByQuery(tagQuery);
        for (PostTag relation : tagRelations) {
            relation.setDeleted(1);
            postTagMapper.update(relation);
        }

        // 逻辑删除与帖子相关的用户行为（点赞、收藏等）
        QueryWrapper actionQuery = QueryWrapper.create()
                .where("target_id = ?", postId)
                .and("target_type = ?", "post")
                .and("(deleted = 0 OR deleted IS NULL)");
        List<UserAction> actions = userActionMapper.selectListByQuery(actionQuery);
        for (UserAction action : actions) {
            action.setDeleted(1);
            userActionMapper.update(action);
        }

        // 逻辑删除投票选项及其用户投票记录
        QueryWrapper optionQuery = QueryWrapper.create()
                .where("post_id = ?", postId)
                .and("(deleted = 0 OR deleted IS NULL)");
        List<PollOption> options = pollOptionMapper.selectListByQuery(optionQuery);
        for (PollOption option : options) {
            option.setDeleted(1);
            pollOptionMapper.update(option);

            QueryWrapper voteQuery = QueryWrapper.create()
                    .where("option_id = ?", option.getId())
                    .and("(deleted = 0 OR deleted IS NULL)");
            List<UserVote> votes = userVoteMapper.selectListByQuery(voteQuery);
            for (UserVote vote : votes) {
                vote.setDeleted(1);
                userVoteMapper.update(vote);
            }
        }
    }
}
