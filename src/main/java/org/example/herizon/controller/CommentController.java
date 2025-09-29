package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.CommentDTO;
import org.example.herizon.dto.CreateCommentRequest;
import org.example.herizon.entity.Comment;
import org.example.herizon.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理控制器
 * <p>
 * 提供评论相关的REST API接口，包括：
 * - 分页查询帖子的评论列表
 * - 创建新评论（支持回复）
 * - 删除评论
 * - 获取用户的评论历史
 * - 获取评论的子评论列表
 * <p>
 * 接口路径前缀：/api/comments
 *
 * @author Kokoa
 */
@Tag(name = "评论管理", description = "评论和回复功能API接口")
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 分页查询帖子的评论列表
     * <p>
     * 获取指定帖子的评论，支持嵌套评论结构
     * 默认按创建时间升序排列，便于按时间顺序阅读
     *
     * @param postId  帖子ID
     * @param current 当前页码，默认1
     * @param size    每页大小，默认20
     * @return 分页的评论DTO列表，包含嵌套回复
     */
    @Operation(summary = "分页查询帖子评论", description = "获取指定帖子的评论列表，支持嵌套回复")
    @GetMapping("/post/{postId}")
    public Result<PageResult<CommentDTO>> getPostComments(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        PageResult<CommentDTO> result = commentService.getPostComments(postId, current, size);
        return Result.success(result);
    }

    /**
     * 获取评论的子评论列表
     * <p>
     * 获取指定评论的所有子评论，用于展开查看回复
     *
     * @param parentId 父评论ID
     * @param current  当前页码，默认1
     * @param size     每页大小，默认10
     * @return 子评论列表
     */
    @Operation(summary = "获取子评论列表", description = "获取指定评论的所有回复")
    @GetMapping("/{parentId}/replies")
    public Result<PageResult<CommentDTO>> getCommentReplies(
            @Parameter(description = "父评论ID") @PathVariable Long parentId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        PageResult<CommentDTO> result = commentService.getCommentReplies(parentId, current, size);
        return Result.success(result);
    }

    /**
     * 创建新评论
     * <p>
     * 用户可以对帖子发表评论，或回复其他评论
     * 如果提供了parentId，则创建回复评论；否则创建顶级评论
     *
     * @param request 创建评论请求，包含帖子ID、评论内容、父评论ID（可选）
     * @param userId  评论者用户ID，从请求头获取
     * @return 创建的评论实体
     */
    @Operation(summary = "创建新评论", description = "用户发表评论或回复其他评论")
    @PostMapping
    public Result<Comment> createComment(
            @Parameter(description = "创建评论请求") @RequestBody CreateCommentRequest request,
            @Parameter(description = "评论者用户ID") @RequestHeader("userId") Long userId) {

        Comment comment = commentService.createComment(request, userId);
        return Result.success(comment);
    }

    /**
     * 删除评论
     * <p>
     * 用户可以删除自己的评论，管理员可以删除任何评论
     * 删除评论时，其子评论会保留但显示为"回复的评论已删除"
     *
     * @param id     评论ID
     * @param userId 操作用户ID，从请求头获取
     * @return 删除结果
     */
    @Operation(summary = "删除评论", description = "用户删除自己的评论，管理员可删除任何评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "操作用户ID") @RequestHeader("userId") Long userId) {

        commentService.deleteComment(id, userId);
        return Result.success();
    }

    /**
     * 获取用户的评论历史
     * <p>
     * 查询指定用户发表的所有评论，按时间倒序排列
     *
     * @param userId  目标用户ID
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @return 用户的评论历史列表
     */
    @Operation(summary = "获取用户评论历史", description = "查询指定用户发表的所有评论")
    @GetMapping("/user/{userId}")
    public Result<PageResult<CommentDTO>> getUserComments(
            @Parameter(description = "目标用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        PageResult<CommentDTO> result = commentService.getUserComments(userId, current, size);
        return Result.success(result);
    }

    /**
     * 获取评论详情
     * <p>
     * 根据ID查询评论的详细信息，包含用户信息和回复统计
     *
     * @param id 评论ID
     * @return 评论详情DTO
     */
    @Operation(summary = "获取评论详情", description = "根据ID查询评论的详细信息")
    @GetMapping("/{id}")
    public Result<CommentDTO> getCommentById(@Parameter(description = "评论ID") @PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        return Result.success(comment);
    }

    /**
     * 获取最新评论
     * <p>
     * 获取全站最新的评论，用于首页或动态展示
     *
     * @param limit 返回数量限制，默认10
     * @return 最新评论列表
     */
    @Operation(summary = "获取最新评论", description = "获取全站最新的评论列表")
    @GetMapping("/latest")
    public Result<List<CommentDTO>> getLatestComments(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {

        List<CommentDTO> comments = commentService.getLatestComments(limit);
        return Result.success(comments);
    }
}