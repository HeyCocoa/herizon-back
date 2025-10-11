package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.CommentDTO;
import org.example.herizon.dto.CreateCommentRequest;
import org.example.herizon.entity.Comment;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.CommentMapper;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论管理服务类
 * <p>
 * 提供评论相关的核心业务功能：
 * - 评论的增删改查操作
 * - 嵌套评论结构处理
 * - 评论权限验证和管理
 * - 评论数据统计和转换
 *
 * @author Kokoa
 */
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询帖子的评论列表
     * <p>
     * 获取指定帖子的顶级评论，每个评论包含少量子评论
     *
     * @param postId  帖子ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，包含评论DTO列表和分页信息
     */
    public PageResult<CommentDTO> getPostComments(Long postId, Integer current, Integer size) {
        // 查询顶级评论（parentId为null的评论）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("post_id = ?", postId)
                .and("parent_id IS NULL")
                .and("status = 0")
                .and("deleted = 0")
                .orderBy("created_at ASC"); // 按时间升序，便于按顺序阅读

        // 执行分页查询
        Page<Comment> page = commentMapper.paginate(Page.of(current, size), queryWrapper);

        // 转换为DTO并加载子评论
        List<CommentDTO> commentDTOs = page.getRecords().stream()
                .map(comment -> convertToDTO(comment, true, 2)) // 加载前2条子评论
                .collect(Collectors.toList());

        return PageResult.of(commentDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 获取评论的子评论列表
     *
     * @param parentId 父评论ID
     * @param current  当前页码
     * @param size     每页大小
     * @return 子评论分页列表
     */
    public PageResult<CommentDTO> getCommentReplies(Long parentId, Integer current, Integer size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("parent_id = ?", parentId)
                .and("status = 0")
                .and("deleted = 0")
                .orderBy("created_at ASC");

        Page<Comment> page = commentMapper.paginate(Page.of(current, size), queryWrapper);

        List<CommentDTO> commentDTOs = page.getRecords().stream()
                .map(comment -> convertToDTO(comment, false, 0)) // 子评论不再加载孙评论
                .collect(Collectors.toList());

        return PageResult.of(commentDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 创建新评论
     *
     * @param request 创建评论请求
     * @param userId  评论者用户ID
     * @return 创建的评论实体
     * @throws RuntimeException 当帖子不存在或父评论无效时抛出
     */
    @Transactional
    public Comment createComment(CreateCommentRequest request, Long userId) {
        // 验证帖子是否存在
        Post post = postMapper.selectOneById(request.getPostId());
        if (post == null || post.getDeleted() == 1 || post.getStatus() != 0) {
            throw new RuntimeException("帖子不存在或已被删除");
        }

        // 如果是回复评论，验证父评论是否存在
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectOneById(request.getParentId());
            if (parentComment == null || parentComment.getDeleted() == 1 || parentComment.getStatus() != 0) {
                throw new RuntimeException("回复的评论不存在或已被删除");
            }
            // 验证父评论是否属于同一帖子
            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new RuntimeException("不能回复其他帖子的评论");
            }
        }

        // 创建评论实体
        // 安全检查：防止必填字段为null导致数据库错误
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        if (request.getPostId() == null) {
            throw new RuntimeException("帖子ID不能为空");
        }

        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setStatus(0);
        comment.setDeleted(0);

        // 保存到数据库
        commentMapper.insert(comment);

        return comment;
    }

    /**
     * 删除评论
     * <p>
     * 用户可以删除自己的评论，管理员可以删除任何评论
     * 如果评论有子评论，则保留为占位符显示
     *
     * @param id     评论ID
     * @param userId 操作用户ID
     * @throws RuntimeException 当评论不存在或无权限时抛出
     */
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectOneById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }

        // 验证权限：用户只能删除自己的评论，管理员可以删除任何评论
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean canDelete = comment.getUserId().equals(userId) || user.getRole() == 2;
        if (!canDelete) {
            throw new RuntimeException("无权限删除此评论");
        }

        // 检查是否有子评论
        QueryWrapper childCountQuery = QueryWrapper.create()
                .where("parent_id = ?", id)
                .and("status = 0")
                .and("deleted = 0");
        long childCount = commentMapper.selectCountByQuery(childCountQuery);

        if (childCount > 0) {
            // 有子评论时，保留评论但标记为删除状态，内容替换为占位符
            comment.setStatus(1);
            comment.setContent("[该评论已被删除]");
        } else {
            // 无子评论时，直接逻辑删除
            comment.setDeleted(1);
        }

        commentMapper.update(comment);
    }

    /**
     * 获取用户的评论历史
     *
     * @param userId  目标用户ID
     * @param current 当前页码
     * @param size    每页大小
     * @return 用户评论历史分页列表
     */
    public PageResult<CommentDTO> getUserComments(Long userId, Integer current, Integer size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("status = 0")
                .and("deleted = 0")
                .orderBy("created_at DESC"); // 按时间倒序，显示最新评论

        Page<Comment> page = commentMapper.paginate(Page.of(current, size), queryWrapper);

        List<CommentDTO> commentDTOs = page.getRecords().stream()
                .map(comment -> convertToDTO(comment, false, 0))
                .collect(Collectors.toList());

        return PageResult.of(commentDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 根据ID获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情DTO，如果不存在返回null
     */
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentMapper.selectOneById(id);
        if (comment == null || comment.getDeleted() == 1) {
            return null;
        }
        return convertToDTO(comment, true, 5); // 加载前5条子评论
    }

    /**
     * 获取最新评论
     *
     * @param limit 返回数量限制
     * @return 最新评论列表
     */
    public List<CommentDTO> getLatestComments(Integer limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("status = 0")
                .and("deleted = 0")
                .orderBy("created_at DESC")
                .limit(limit);

        List<Comment> comments = commentMapper.selectListByQuery(queryWrapper);
        return comments.stream()
                .map(comment -> convertToDTO(comment, false, 0))
                .collect(Collectors.toList());
    }

    /**
     * 将评论实体转换为DTO
     *
     * @param comment           评论实体
     * @param includeReplies    是否包含子评论
     * @param maxReplies        最多包含的子评论数量
     * @return 评论DTO
     */
    private CommentDTO convertToDTO(Comment comment, boolean includeReplies, int maxReplies) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 获取评论者用户名
        User user = userMapper.selectOneById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
        }

        // 获取所属帖子标题，安全处理可能为null的标题字段
        Post post = postMapper.selectOneById(comment.getPostId());
        if (post != null) {
            dto.setPostTitle(post.getTitle() != null ? post.getTitle() : "");
        }

        // 如果是回复评论，获取父评论作者用户名
        if (comment.getParentId() != null) {
            Comment parentComment = commentMapper.selectOneById(comment.getParentId());
            if (parentComment != null) {
                User parentUser = userMapper.selectOneById(parentComment.getUserId());
                if (parentUser != null) {
                    dto.setParentUsername(parentUser.getUsername());
                }
            }
        }

        // 计算回复数量
        QueryWrapper replyCountQuery = QueryWrapper.create()
                .where("parent_id = ?", comment.getId())
                .and("status = 0")
                .and("deleted = 0");
        int replyCount = (int) commentMapper.selectCountByQuery(replyCountQuery);
        dto.setReplyCount(replyCount);

        // 计算层级深度
        dto.setLevel(calculateCommentLevel(comment));

        // 检查是否为占位符（已删除但有子评论的评论）
        dto.setIsPlaceholder(comment.getStatus() == 1);

        // 如果需要包含子评论，加载子评论列表
        if (includeReplies && maxReplies > 0 && replyCount > 0) {
            QueryWrapper repliesQuery = QueryWrapper.create()
                    .where("parent_id = ?", comment.getId())
                    .and("status = 0")
                    .and("deleted = 0")
                    .orderBy("created_at ASC")
                    .limit(maxReplies);

            List<Comment> replies = commentMapper.selectListByQuery(repliesQuery);
            List<CommentDTO> replyDTOs = replies.stream()
                    .map(reply -> convertToDTO(reply, false, 0)) // 子评论不再递归加载
                    .collect(Collectors.toList());
            dto.setReplies(replyDTOs);
        } else {
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }

    /**
     * 计算评论的层级深度
     *
     * @param comment 评论实体
     * @return 层级深度，0表示顶级评论
     */
    private Integer calculateCommentLevel(Comment comment) {
        if (comment.getParentId() == null) {
            return 0; // 顶级评论
        }

        // 递归查找父评论计算层级
        int level = 0;
        Long currentParentId = comment.getParentId();
        while (currentParentId != null && level < 10) { // 限制最大层级防止死循环
            Comment parentComment = commentMapper.selectOneById(currentParentId);
            if (parentComment == null) {
                break;
            }
            level++;
            currentParentId = parentComment.getParentId();
        }

        return level;
    }
}