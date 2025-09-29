package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.Result;
import org.example.herizon.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户行为管理
 * <p>
 * 提供统一的用户行为操作API，包括：
 * - 点赞/取消点赞
 * - 收藏/取消收藏
 * - 分享
 * - 举报
 * <p>
 * 接口路径前缀：/api/actions
 *
 * @author Kokoa
 */
@Tag(name = "用户行为管理", description = "点赞、收藏、分享、举报等用户行为API")
@RestController
@RequestMapping ("/actions")
public class ActionController{

    @Autowired
    private ActionService actionService;

    /**
     * 切换点赞状态
     * <p>
     * 如果用户已点赞则取消点赞，未点赞则添加点赞
     *
     * @param targetId   目标对象ID（如帖子ID、评论ID）
     * @param targetType 目标对象类型，默认"post"
     * @param userId     操作用户ID，从请求头获取
     * @return 点赞状态：true=已点赞，false=已取消点赞
     */
    @Operation(summary = "切换点赞状态", description = "如果已点赞则取消，未点赞则添加")
    @PostMapping ("/like")
    public Result<Boolean> toggleLike(
            @Parameter(description = "目标对象ID") @RequestParam Long targetId,
            @Parameter(description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
            @Parameter(description = "操作用户ID") @RequestHeader ("userId") Long userId){

        boolean isLiked = actionService.toggleLike(userId, targetId, targetType);
        return Result.success(isLiked);
    }

    /**
     * 切换收藏状态
     * <p>
     * 如果用户已收藏则取消收藏，未收藏则添加收藏
     *
     * @param targetId   目标对象ID（如帖子ID、评论ID）
     * @param targetType 目标对象类型，默认"post"
     * @param userId     操作用户ID，从请求头获取
     * @return 收藏状态：true=已收藏，false=已取消收藏
     */
    @Operation(summary = "切换收藏状态", description = "如果已收藏则取消，未收藏则添加")
    @PostMapping ("/collect")
    public Result<Boolean> toggleCollect(
            @Parameter(description = "目标对象ID") @RequestParam Long targetId,
            @Parameter(description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
            @Parameter(description = "操作用户ID") @RequestHeader ("userId") Long userId){

        boolean isCollected = actionService.toggleCollect(userId, targetId, targetType);
        return Result.success(isCollected);
    }

    /**
     * 分享内容
     * <p>
     * 记录用户分享行为，增加目标对象的分享计数
     *
     * @param targetId   目标对象ID（如帖子ID、评论ID）
     * @param targetType 目标对象类型，默认"post"
     * @param userId     操作用户ID，从请求头获取
     * @return 成功响应
     */
    @Operation(summary = "分享内容", description = "记录分享行为，增加分享计数")
    @PostMapping ("/share")
    public Result<Void> share(
            @Parameter(description = "目标对象ID") @RequestParam Long targetId,
            @Parameter(description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
            @Parameter(description = "操作用户ID") @RequestHeader ("userId") Long userId){

        actionService.share(userId, targetId, targetType);
        return Result.success();
    }

    /**
     * 举报内容
     * <p>
     * 用户举报不当内容，记录举报行为和举报原因
     *
     * @param targetId   目标对象ID（如帖子ID、评论ID）
     * @param targetType 目标对象类型，默认"post"
     * @param reason     举报原因
     * @param userId     操作用户ID，从请求头获取
     * @return 成功响应
     */
    @Operation(summary = "举报内容", description = "举报不当内容，记录举报原因")
    @PostMapping ("/report")
    public Result<Void> report(
            @Parameter(description = "目标对象ID") @RequestParam Long targetId,
            @Parameter(description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
            @Parameter(description = "举报原因") @RequestParam String reason,
            @Parameter(description = "操作用户ID") @RequestHeader ("userId") Long userId){

        actionService.report(userId, targetId, targetType, reason);
        return Result.success();
    }
}