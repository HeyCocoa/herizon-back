package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.CreatePostRequest;
import org.example.herizon.dto.PostDTO;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.PostTag;
import org.example.herizon.entity.Tag;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username")
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
        List<PostDTO> postDTOs = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("posts.*, users.username")
                .from("posts")
                .leftJoin("users").on("posts.user_id = users.id")
                .where("posts.status = 0")
                // 使用EXISTS子查询筛选标签
                .and("EXISTS (SELECT 1 FROM post_tags pt WHERE pt.post_id = posts.id AND pt.tag_id = ? AND pt.deleted = 0)", tagId)
                // 按时间降序排序显示最新内容
                .orderBy("posts.created_at DESC");

        // 执行分页查询
        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO
        List<PostDTO> postDTOs = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
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

        // 自动增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postMapper.update(post);

        PostDTO postDTO = convertToDTO(post);

        // 如果提供了当前用户ID，查询用户对该帖子的操作状态
        if( currentUserId!=null ){
            postDTO.setIsLiked(hasUserAction(currentUserId, id, "post", 0));
            postDTO.setIsCollected(hasUserAction(currentUserId, id, "post", 1));
        }

        return postDTO;
    }

    /**
     * 创建新帖子
     *
     * @param request 创建帖子请求，包含标题、内容、类型、标签等
     * @param userId  创建者用户ID
     * @return 创建的帖子实体
     */
    @Transactional
    public Post createPost(CreatePostRequest request, Long userId){
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPostType(request.getPostType());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setShareCount(0);
        post.setCollectCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setStatus(0);

        postMapper.insert(post);

        // 处理帖子标签：用户只能选择已有标签，不能创建新标签
        if( request.getTags()!=null && !request.getTags().isEmpty() ){
            for( String tagName : request.getTags() ){
                Tag tag = getExistingTag(tagName);  // 只获取已存在的标签，不创建新标签

                PostTag postTag = new PostTag();
                postTag.setPostId(post.getId());
                postTag.setTagId(tag.getId());
                postTag.setDeleted(0);  // 设置未删除状态
                postTagMapper.insert(postTag);
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

        if( tag == null ){
            throw new RuntimeException("标签 '" + tagName + "' 不存在，请选择已有标签或联系管理员创建");
        }

        return tag;
    }

    /**
     * 将帖子实体转换为DTO
     *
     * @param post 帖子实体
     * @return 帖子DTO，包含用户名和标签信息
     */
    private PostDTO convertToDTO(Post post){
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);

        // 获取发帖用户的用户名
        User user = userMapper.selectOneById(post.getUserId());
        if( user!=null ){
            dto.setUsername(user.getUsername());
        }

        // 获取帖子关联的所有标签
        List<String> tags = getPostTags(post.getId());
        dto.setTags(tags);

        return dto;
    }

    /**
     * 获取帖子的标签列表
     *
     * @param postId 帖子ID
     * @return 标签名称列表
     */
    private List<String> getPostTags(Long postId){
        // 查询指定帖子的所有标签关联记录
        // 只查询未删除的记录，避免显示已删除的标签关联
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("post_id = ?", postId)    // 按帖子ID筛选
                .and("deleted = 0");             // 逻辑删除过滤

        // 获取帖子-标签关联记录列表
        List<PostTag> postTags = postTagMapper.selectListByQuery(queryWrapper);

        // 将PostTag实体转换为标签名称列表
        // 采用分步查询避免复杂的JOIN操作和MyBatis-Flex泛型兼容性问题
        return postTags.stream()
                .map(pt->{
                    // 根据标签ID查询标签实体
                    Tag tag = tagMapper.selectOneById(pt.getTagId());
                    return tag!=null ? tag.getName() : null;  // 防空处理
                })
                .filter(Objects::nonNull)   // 过滤空值，确保返回有效标签名
                .collect(Collectors.toList());  // 收集为List<String>
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
}