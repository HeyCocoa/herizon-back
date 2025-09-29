package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.Result;
import org.example.herizon.dto.UserRegistrationRequest;
import org.example.herizon.dto.UserLoginRequest;
import org.example.herizon.dto.UserProfileDTO;
import org.example.herizon.entity.User;
import org.example.herizon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * <p>
 * 提供用户相关的REST API接口，包括：
 * - 用户注册（包含身份认证问卷）
 * - 用户登录验证
 * - 用户资料查询和更新
 * - 用户角色管理
 * <p>
 * 接口路径前缀：/api/users
 *
 * @author Kokoa
 */
@Tag(name = "用户管理", description = "用户注册、登录、资料管理等API接口")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * <p>
     * 新用户注册，默认为体验用户角色(role=0)
     * 需要填写身份认证问卷以便后续升级为正式用户
     *
     * @param request 注册请求，包含用户名、邮箱、密码、问卷数据
     * @return 注册成功的用户信息
     */
    @Operation(summary = "用户注册", description = "新用户注册，包含身份认证问卷")
    @PostMapping("/register")
    public Result<User> register(@Parameter(description = "注册请求") @RequestBody UserRegistrationRequest request) {
        User user = userService.register(request);
        return Result.success(user);
    }

    /**
     * 用户登录
     * <p>
     * 用户身份验证，支持用户名或邮箱登录
     *
     * @param request 登录请求，包含用户名/邮箱和密码
     * @return 登录成功的用户信息和会话token
     */
    @Operation(summary = "用户登录", description = "用户身份验证，支持用户名或邮箱登录")
    @PostMapping("/login")
    public Result<UserProfileDTO> login(@Parameter(description = "登录请求") @RequestBody UserLoginRequest request) {
        UserProfileDTO userProfile = userService.login(request);
        return Result.success(userProfile);
    }

    /**
     * 获取用户资料
     * <p>
     * 查询指定用户的公开资料信息
     *
     * @param userId 用户ID
     * @return 用户资料DTO
     */
    @Operation(summary = "获取用户资料", description = "查询用户的公开资料信息")
    @GetMapping("/{userId}")
    public Result<UserProfileDTO> getUserProfile(@Parameter(description = "用户ID") @PathVariable Long userId) {
        UserProfileDTO profile = userService.getUserProfile(userId);
        if (profile == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(profile);
    }

    /**
     * 获取当前用户资料
     * <p>
     * 查询当前登录用户的完整资料，包含私有信息
     *
     * @param currentUserId 当前用户ID，从请求头获取
     * @return 当前用户的完整资料
     */
    @Operation(summary = "获取当前用户资料", description = "查询当前登录用户的完整资料")
    @GetMapping("/me")
    public Result<UserProfileDTO> getCurrentUserProfile(
            @Parameter(description = "当前用户ID") @RequestHeader("userId") Long currentUserId) {
        UserProfileDTO profile = userService.getCurrentUserProfile(currentUserId);
        return Result.success(profile);
    }

    /**
     * 更新用户资料
     * <p>
     * 用户可以更新自己的基本信息，但不能修改角色等敏感字段
     *
     * @param currentUserId 当前用户ID，从请求头获取
     * @param request       更新请求，包含需要更新的字段
     * @return 更新后的用户资料
     */
    @Operation(summary = "更新用户资料", description = "用户更新自己的基本信息")
    @PutMapping("/me")
    public Result<UserProfileDTO> updateProfile(
            @Parameter(description = "当前用户ID") @RequestHeader("userId") Long currentUserId,
            @Parameter(description = "更新请求") @RequestBody UserRegistrationRequest request) {
        UserProfileDTO profile = userService.updateUserProfile(currentUserId, request);
        return Result.success(profile);
    }

    /**
     * 申请身份认证
     * <p>
     * 体验用户提交身份认证申请，升级为正式用户
     *
     * @param currentUserId 当前用户ID，从请求头获取
     * @param questionnaireData 身份认证问卷数据
     * @return 申请结果
     */
    @Operation(summary = "申请身份认证", description = "体验用户申请升级为正式用户")
    @PostMapping("/verify")
    public Result<Void> applyVerification(
            @Parameter(description = "当前用户ID") @RequestHeader("userId") Long currentUserId,
            @Parameter(description = "身份认证问卷数据") @RequestBody String questionnaireData) {
        userService.applyVerification(currentUserId, questionnaireData);
        return Result.success();
    }

    /**
     * 检查用户名是否可用
     * <p>
     * 注册前检查用户名是否已被占用
     *
     * @param username 待检查的用户名
     * @return 是否可用：true=可用，false=已占用
     */
    @Operation(summary = "检查用户名可用性", description = "注册前检查用户名是否已被占用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@Parameter(description = "待检查的用户名") @RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return Result.success(available);
    }

    /**
     * 检查邮箱是否可用
     * <p>
     * 注册前检查邮箱是否已被注册
     *
     * @param email 待检查的邮箱
     * @return 是否可用：true=可用，false=已注册
     */
    @Operation(summary = "检查邮箱可用性", description = "注册前检查邮箱是否已被注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@Parameter(description = "待检查的邮箱") @RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return Result.success(available);
    }

}