package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.AdminUserDTO;
import org.example.herizon.dto.PostDTO;
import org.example.herizon.dto.VerifyUserRequest;
import org.example.herizon.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器（简化版 - 符合MVP原则）
 * <p>
 * 提供管理员专用的核心功能，包括：
 * - 用户身份认证审核（体验用户升级为正式用户）
 * - 平台基础数据统计
 * <p>
 * 标签管理功能复用TagController的现有API
 * <p>
 * 接口路径前缀：/api/admin
 * 注意：所有接口都需要管理员权限（role=2）
 * <p>
 * 简化说明（2025-10-02）：
 * 删除了过度设计的功能（举报处理、帖子管理、用户管理等），
 * 仅保留核心的用户审核和统计功能，符合最小可行产品原则
 *
 * @author Kokoa
 */
@Tag(name = "管理员功能", description = "管理员专用的审核和管理功能API（简化版）")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取待审核的用户身份认证申请
     * <p>
     * 查询所有提交了身份认证问卷但尚未审核的用户
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @param adminId 管理员ID，从请求头获取
     * @return 待审核用户列表
     */
    @Operation(summary = "获取待审核用户列表", description = "查询提交身份认证申请但尚未审核的用户")
    @GetMapping("/users/pending")
    public Result<PageResult<AdminUserDTO>> getPendingUsers(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<AdminUserDTO> result = adminService.getPendingUsers(current, size, adminId);
        return Result.success(result);
    }

    /**
     * 审核用户身份认证申请
     * <p>
     * 管理员审核用户的身份认证申请，决定是否通过
     * <p>
     * 修复说明：将参数接收方式从@RequestParam改为@RequestBody，
     * 以匹配前端发送的JSON请求体格式
     *
     * @param userId   待审核用户ID
     * @param request  审核请求参数（包含approved和reason字段）
     * @param adminId  管理员ID，从请求头获取
     * @return 审核结果
     */
    @Operation(summary = "审核用户身份认证", description = "管理员审核用户身份认证申请")
    @PostMapping("/users/{userId}/verify")
    public Result<Void> verifyUser(
            @Parameter(description = "待审核用户ID") @PathVariable Long userId,
            @Parameter(description = "审核请求参数") @RequestBody VerifyUserRequest request,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.verifyUser(userId, request.getApproved(), request.getReason(), adminId);
        return Result.success();
    }

    /**
     * 管理员将普通用户提升为管理员
     *
     * @param userId  被提升的用户ID
     * @param adminId 操作管理员ID
     * @return 操作结果
     */
    @Operation(summary = "将用户设为管理员", description = "管理员赋予其他用户管理员权限")
    @PostMapping("/users/{userId}/promote")
    public Result<Void> promoteUser(
            @Parameter(description = "目标用户ID") @PathVariable Long userId,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.promoteUserToAdmin(userId, adminId);
        return Result.success();
    }

    /**
     * 管理员删除用户（逻辑删除）
     *
     * @param userId  被删除的用户ID
     * @param adminId 操作管理员ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "管理员逻辑删除指定用户，并同步清理其帖子")
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(
            @Parameter(description = "目标用户ID") @PathVariable Long userId,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.deleteUser(userId, adminId);
        return Result.success();
    }

    /**
     * 管理员分页查询所有用户
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param adminId 管理员ID
     * @return 用户分页列表
     */
    @Operation(summary = "查询全量用户", description = "管理员分页查看所有用户信息")
    @GetMapping("/users")
    public Result<PageResult<AdminUserDTO>> getAllUsers(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<AdminUserDTO> result = adminService.getAllUsers(current, size, adminId);
        return Result.success(result);
    }

    /**
     * 管理员分页查询所有帖子
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param adminId 管理员ID
     * @return 分页帖子列表
     */
    @Operation(summary = "查询全站帖子", description = "管理员分页查看所有用户发布的帖子")
    @GetMapping("/posts")
    public Result<PageResult<PostDTO>> getAllPosts(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<PostDTO> result = adminService.getAllPosts(current, size, adminId);
        return Result.success(result);
    }

    /**
     * 管理员删除任意用户的帖子
     *
     * @param postId  帖子ID
     * @param adminId 操作管理员ID
     * @return 操作结果
     */
    @Operation(summary = "删除帖子", description = "管理员删除任意用户的帖子，采用逻辑删除")
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.deletePost(postId, adminId);
        return Result.success();
    }

    /**
     * 获取平台统计数据
     * <p>
     * 获取平台的基本统计信息，用于管理员仪表板
     *
     * @param adminId 管理员ID，从请求头获取
     * @return 平台统计数据
     */
    @Operation(summary = "获取平台统计数据", description = "获取用户、帖子、评论等统计信息")
    @GetMapping("/statistics")
    public Result<Object> getStatistics(@Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {
        Object statistics = adminService.getStatistics(adminId);
        return Result.success(statistics);
    }

}
