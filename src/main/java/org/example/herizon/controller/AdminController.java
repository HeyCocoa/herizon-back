package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.AdminReportDTO;
import org.example.herizon.dto.AdminUserDTO;
import org.example.herizon.dto.AdminPostDTO;
import org.example.herizon.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 * <p>
 * 提供管理员专用的内容审核和管理功能，包括：
 * - 用户身份认证审核
 * - 举报内容审核和处理
 * - 用户管理（角色变更、封禁等）
 * - 内容管理（删除违规内容等）
 * - 平台数据统计
 * <p>
 * 接口路径前缀：/api/admin
 * 注意：所有接口都需要管理员权限（role=2）
 *
 * @author Kokoa
 */
@Tag(name = "管理员功能", description = "管理员专用的审核和管理功能API")
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
     *
     * @param userId   待审核用户ID
     * @param approved 是否通过：true=通过并升级为正式用户，false=拒绝申请
     * @param reason   审核意见或拒绝原因
     * @param adminId  管理员ID，从请求头获取
     * @return 审核结果
     */
    @Operation(summary = "审核用户身份认证", description = "管理员审核用户身份认证申请")
    @PostMapping("/users/{userId}/verify")
    public Result<Void> verifyUser(
            @Parameter(description = "待审核用户ID") @PathVariable Long userId,
            @Parameter(description = "是否通过审核") @RequestParam Boolean approved,
            @Parameter(description = "审核意见") @RequestParam(required = false) String reason,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.verifyUser(userId, approved, reason, adminId);
        return Result.success();
    }

    /**
     * 获取待处理的举报列表
     * <p>
     * 查询所有待处理的用户举报，按时间倒序排列
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @param adminId 管理员ID，从请求头获取
     * @return 待处理举报列表
     */
    @Operation(summary = "获取待处理举报列表", description = "查询所有待处理的用户举报")
    @GetMapping("/reports/pending")
    public Result<PageResult<AdminReportDTO>> getPendingReports(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<AdminReportDTO> result = adminService.getPendingReports(current, size, adminId);
        return Result.success(result);
    }

    /**
     * 处理举报
     * <p>
     * 管理员处理用户举报，决定是否删除被举报内容
     *
     * @param reportId 举报记录ID
     * @param action   处理动作：approve=删除被举报内容，reject=拒绝举报
     * @param reason   处理理由
     * @param adminId  管理员ID，从请求头获取
     * @return 处理结果
     */
    @Operation(summary = "处理举报", description = "管理员处理用户举报，决定是否删除被举报内容")
    @PostMapping("/reports/{reportId}/handle")
    public Result<Void> handleReport(
            @Parameter(description = "举报记录ID") @PathVariable Long reportId,
            @Parameter(description = "处理动作") @RequestParam String action,
            @Parameter(description = "处理理由") @RequestParam(required = false) String reason,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.handleReport(reportId, action, reason, adminId);
        return Result.success();
    }

    /**
     * 管理员删除帖子
     * <p>
     * 管理员可以直接删除违规帖子，并可选择发布违规公示
     *
     * @param postId         帖子ID
     * @param reason         删除原因
     * @param publishNotice  是否发布违规公示帖
     * @param adminId        管理员ID，从请求头获取
     * @return 删除结果
     */
    @Operation(summary = "管理员删除帖子", description = "管理员直接删除违规帖子，可选择发布违规公示")
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "删除原因") @RequestParam String reason,
            @Parameter(description = "是否发布违规公示") @RequestParam(defaultValue = "false") Boolean publishNotice,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.deletePost(postId, reason, publishNotice, adminId);
        return Result.success();
    }

    /**
     * 修改用户角色
     * <p>
     * 管理员可以修改用户的角色（体验用户、正式用户、管理员）
     *
     * @param userId  目标用户ID
     * @param newRole 新角色：0=体验用户，1=正式用户，2=管理员
     * @param adminId 管理员ID，从请求头获取
     * @return 修改结果
     */
    @Operation(summary = "修改用户角色", description = "管理员修改用户的角色权限")
    @PutMapping("/users/{userId}/role")
    public Result<Void> changeUserRole(
            @Parameter(description = "目标用户ID") @PathVariable Long userId,
            @Parameter(description = "新角色") @RequestParam Integer newRole,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        adminService.changeUserRole(userId, newRole, adminId);
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

    /**
     * 获取所有用户列表
     * <p>
     * 管理员查看所有用户，支持按角色筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认20
     * @param role    角色筛选，可选
     * @param adminId 管理员ID，从请求头获取
     * @return 用户列表
     */
    @Operation(summary = "获取所有用户列表", description = "管理员查看所有用户，支持按角色筛选")
    @GetMapping("/users")
    public Result<PageResult<AdminUserDTO>> getAllUsers(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "角色筛选") @RequestParam(required = false) Integer role,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<AdminUserDTO> result = adminService.getAllUsers(current, size, role, adminId);
        return Result.success(result);
    }

    /**
     * 获取所有帖子列表
     * <p>
     * 管理员查看所有帖子，支持按状态筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认20
     * @param status  状态筛选，可选
     * @param adminId 管理员ID，从请求头获取
     * @return 帖子列表
     */
    @Operation(summary = "获取所有帖子列表", description = "管理员查看所有帖子，支持按状态筛选")
    @GetMapping("/posts")
    public Result<PageResult<AdminPostDTO>> getAllPosts(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {

        PageResult<AdminPostDTO> result = adminService.getAllPosts(current, size, status, adminId);
        return Result.success(result);
    }
}