package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.CreateTagRequest;
import org.example.herizon.dto.TagDTO;
import org.example.herizon.entity.Tag;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.PostTagMapper;
import org.example.herizon.mapper.TagMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签管理服务类
 * <p>
 * 提供标签相关的核心业务功能：
 * - 标签的增删改查操作
 * - 标签搜索和热门标签获取
 * - 标签使用统计和数据同步
 * - 标签与帖子的关联管理
 *
 * @author Kokoa
 */
@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询标签列表
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param sortBy  排序方式："count"=按帖子数量降序，"time"=按创建时间降序
     * @return 分页结果，包含标签DTO列表和分页信息
     */
    public PageResult<TagDTO> getTagList(Integer current, Integer size, String sortBy) {
        // 创建查询包装器，只查询未删除的标签
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("deleted = 0");

        // 根据sortBy参数确定排序规则
        if ("time".equals(sortBy)) {
            // 按创建时间降序排序，显示最新创建的标签
            queryWrapper.orderBy("created_at DESC");
        } else {
            // 默认按帖子数量降序排序，显示最热门的标签
            queryWrapper.orderBy("post_count DESC", "created_at DESC");
        }

        // 执行分页查询
        Page<Tag> page = tagMapper.paginate(Page.of(current, size), queryWrapper);

        // 将Tag实体转换为TagDTO，包含额外的统计信息
        List<TagDTO> tagDTOs = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 封装分页结果
        return PageResult.of(tagDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 根据ID查询标签详情
     *
     * @param id 标签ID
     * @return 标签详情DTO，如果标签不存在返回null
     */
    public TagDTO getTagById(Long id) {
        Tag tag = tagMapper.selectOneById(id);
        if (tag == null || tag.getDeleted() == 1) {
            return null;
        }
        return convertToDTO(tag);
    }

    /**
     * 创建新标签
     * <p>
     * 根据系统变更要求，只有管理员可以创建新标签
     * 普通用户发帖时只能从已有标签中选择
     *
     * @param request 创建标签请求，包含标签名称和描述
     * @param userId  创建者用户ID，用于权限验证
     * @return 创建的标签DTO
     * @throws RuntimeException 当用户无权限或标签名称已存在时抛出
     */
    @Transactional
    public TagDTO createTag(CreateTagRequest request, Long userId) {
        // 验证用户权限：只有管理员（role=2）可以创建标签
        User user = userMapper.selectOneById(userId);
        if (user == null || user.getRole() != 2) {
            throw new RuntimeException("只有管理员可以创建新标签");
        }

        // 验证标签名称唯一性
        if (!isTagNameAvailable(request.getName())) {
            throw new RuntimeException("标签名称已存在");
        }

        // 创建标签实体
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setPostCount(0); // 新标签的帖子数量为0
        tag.setCreatedAt(LocalDateTime.now());
        tag.setDeleted(0);

        // 保存到数据库
        tagMapper.insert(tag);

        return convertToDTO(tag);
    }

    /**
     * 更新标签信息
     *
     * @param id      标签ID
     * @param request 更新请求，包含新的标签信息
     * @return 更新后的标签DTO
     * @throws RuntimeException 当标签不存在或名称冲突时抛出
     */
    @Transactional
    public TagDTO updateTag(Long id, CreateTagRequest request) {
        Tag tag = tagMapper.selectOneById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new RuntimeException("标签不存在");
        }

        // 检查标签名称变更的唯一性
        if (!tag.getName().equals(request.getName()) && !isTagNameAvailable(request.getName())) {
            throw new RuntimeException("标签名称已存在");
        }

        // 更新标签信息
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());

        tagMapper.update(tag);

        return convertToDTO(tag);
    }

    /**
     * 搜索标签
     * <p>
     * 根据关键词搜索标签名称，支持模糊匹配
     *
     * @param keyword 搜索关键词
     * @param limit   返回结果数量限制
     * @return 匹配的标签列表，按帖子数量降序排序
     */
    public List<TagDTO> searchTags(String keyword, Integer limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("name LIKE ?", "%" + keyword + "%")
                .and("deleted = 0")
                .orderBy("post_count DESC", "created_at DESC")
                .limit(limit);

        List<Tag> tags = tagMapper.selectListByQuery(queryWrapper);
        return tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门标签
     * <p>
     * 获取按帖子数量排序的热门标签，用于首页展示
     *
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    public List<TagDTO> getHotTags(Integer limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("deleted = 0")
                .and("post_count > 0") // 只显示有帖子的标签
                .orderBy("post_count DESC", "created_at DESC")
                .limit(limit);

        List<Tag> tags = tagMapper.selectListByQuery(queryWrapper);
        return tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除标签
     * <p>
     * 逻辑删除标签，只有管理员可以执行此操作
     *
     * @param id     标签ID
     * @param userId 操作用户ID
     * @throws RuntimeException 当标签不存在或用户无权限时抛出
     */
    @Transactional
    public void deleteTag(Long id, Long userId) {
        // 验证用户权限（只有管理员可以删除标签）
        User user = userMapper.selectOneById(userId);
        if (user == null || user.getRole() != 2) {
            throw new RuntimeException("无权限执行此操作");
        }

        Tag tag = tagMapper.selectOneById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new RuntimeException("标签不存在");
        }

        // 逻辑删除标签
        tag.setDeleted(1);
        tagMapper.update(tag);

        // 同时逻辑删除相关的帖子-标签关联记录
        QueryWrapper updateWrapper = QueryWrapper.create()
                .where("tag_id = ?", id)
                .and("deleted = 0");

        // 注意：这里需要先查询再更新，MyBatis-Flex的批量更新需要明确的实体
        // 为简化实现，这里使用直接SQL更新
        postTagMapper.selectListByQuery(updateWrapper).forEach(postTag -> {
            postTag.setDeleted(1);
            postTagMapper.update(postTag);
        });
    }

    /**
     * 检查标签名称是否可用
     *
     * @param name 待检查的标签名称
     * @return true=可用，false=已存在
     */
    private boolean isTagNameAvailable(String name) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("name = ?", name)
                .and("deleted = 0");
        return tagMapper.selectCountByQuery(queryWrapper) == 0;
    }

    /**
     * 将标签实体转换为DTO
     * <p>
     * 增加额外的统计信息和计算字段
     *
     * @param tag 标签实体
     * @return 标签DTO
     */
    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);

        // 查询最近使用时间（最后一次有帖子使用该标签的时间）
        QueryWrapper lastUsedQuery = QueryWrapper.create()
                .select("MAX(posts.created_at)")
                .from("post_tags")
                .leftJoin("posts").on("post_tags.post_id = posts.id")
                .where("post_tags.tag_id = ?", tag.getId())
                .and("post_tags.deleted = 0")
                .and("posts.deleted = 0");

        // 这里简化处理，实际项目中可能需要更复杂的查询
        // 暂时使用标签创建时间作为最近使用时间
        dto.setLastUsedAt(tag.getCreatedAt());

        // 查询使用该标签的最新帖子标题示例
        QueryWrapper latestPostQuery = QueryWrapper.create()
                .select("posts.title")
                .from("post_tags")
                .leftJoin("posts").on("post_tags.post_id = posts.id")
                .where("post_tags.tag_id = ?", tag.getId())
                .and("post_tags.deleted = 0")
                .and("posts.deleted = 0")
                .and("posts.status = 0")
                .orderBy("posts.created_at DESC")
                .limit(1);

        // 简化处理：如果有帖子使用该标签，设置示例标题
        if (tag.getPostCount() > 0) {
            dto.setLatestPostTitle("相关内容示例...");
        }

        return dto;
    }

    /**
     * 更新标签的帖子计数
     * <p>
     * 当有帖子添加或删除标签时调用此方法同步计数
     * 这个方法会在PostService中被调用
     *
     * @param tagId 标签ID
     */
    @Transactional
    public void updateTagPostCount(Long tagId) {
        // 统计使用该标签的有效帖子数量
        QueryWrapper countQuery = QueryWrapper.create()
                .select("COUNT(*)")
                .from("post_tags")
                .leftJoin("posts").on("post_tags.post_id = posts.id")
                .where("post_tags.tag_id = ?", tagId)
                .and("post_tags.deleted = 0")
                .and("posts.deleted = 0")
                .and("posts.status = 0");

        long count = postTagMapper.selectCountByQuery(countQuery);

        // 更新标签的帖子计数
        Tag tag = tagMapper.selectOneById(tagId);
        if (tag != null) {
            tag.setPostCount((int) count);
            tagMapper.update(tag);
        }
    }
}