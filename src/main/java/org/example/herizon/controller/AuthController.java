package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.herizon.common.Result;
import org.example.herizon.dto.Code2SessionResponse;
import org.example.herizon.dto.WechatLoginRequest;
import org.example.herizon.dto.WechatLoginResponse;
import org.example.herizon.service.UserService;
import org.example.herizon.service.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 处理所有用户身份认证相关的请求，包括：
 * - 微信小程序登录
 * - Token验证
 * - Token刷新
 * - 登录状态检查
 * <p>
 * 接口路径前缀：/api/auth
 *
 * @author Kokoa
 */
@Tag (name = "用户认证", description = "微信登录、Token验证等认证相关API接口")
@RestController
@RequestMapping ("/auth")
public class AuthController{

    @Autowired
    private WechatService wechatService;

    @Autowired
    private UserService userService;


    /**
     * 微信小程序登录
     * <p>
     * 完整的微信登录流程：
     * 1. 接收前端传来的微信code
     * 2. 调用微信API验证code并获取openid、session_key
     * 3. 根据openid查询用户，不存在则自动注册
     * 4. 生成简单Token返回给前端
     *
     * @param request 微信登录请求，包含微信code和用户信息
     * @return 登录响应，包含Token和用户信息
     */
    @Operation (summary = "微信小程序登录", description = "通过微信code进行登录，支持自动注册新用户")
    @PostMapping ("/wechat-login")
    public Result<WechatLoginResponse> wechatLogin(
            @Parameter (description = "微信登录请求") @RequestBody WechatLoginRequest request){

        try {
            // 步骤1: 调用微信API验证code
            Code2SessionResponse wxResponse = wechatService.code2Session(request.getCode());

            // 步骤2: 根据openid进行用户登录或注册
            WechatLoginResponse loginResponse = userService.wechatLogin(
                    wxResponse.getOpenid(),
                    wxResponse.getSessionKey(),
                    wxResponse.getUnionid(),
                    request
            );

            return Result.success(loginResponse);

        } catch( Exception e ) {
            System.err.println("微信登录失败: " + e.getMessage());
            e.printStackTrace();

            // 根据异常类型返回不同的错误信息
            String errorMessage = "登录失败";
            if( e.getMessage()!=null ){
                if( e.getMessage().contains("code") ){
                    errorMessage = "微信授权码无效，请重新登录";
                }
                else if( e.getMessage().contains("配置") ){
                    errorMessage = "系统配置错误，请联系管理员";
                }
                else if( e.getMessage().contains("网络") ){
                    errorMessage = "网络连接失败，请检查网络后重试";
                }
                else{
                    errorMessage = e.getMessage();
                }
            }

            return Result.error(500, errorMessage);
        }
    }

    /**
     * 验证Token有效性
     * <p>
     * 检查客户端提供的Token是否有效
     * 用于前端判断登录状态和接口权限验证
     *
     * @param request HTTP请求，从Authorization头获取Token
     * @return 验证结果
     */
    @Operation (summary = "验证Token有效性", description = "检查Token是否有效，用于登录状态验证")
    @PostMapping ("/validate-token")
    public Result<Void> validateToken(HttpServletRequest request){
        try {
            // 从请求头获取Token
            String token = extractTokenFromRequest(request);

            if( token==null ){
                return Result.error(401, "Token缺失，请重新登录");
            }

            // 验证Token有效性
            if( !userService.validateToken(token) ){
                return Result.error(401, "Token无效或已过期，请重新登录");
            }

            return Result.success();

        } catch( Exception e ) {
            System.err.println("Token验证失败: " + e.getMessage());
            return Result.error(401, "Token验证失败，请重新登录");
        }
    }

    /**
     * 刷新Token
     * <p>
     * 为即将过期的Token生成新的Token
     * 延长用户登录状态，避免频繁重新登录
     *
     * @param request HTTP请求，从Authorization头获取原Token
     * @return 新的Token
     */
    @Operation (summary = "刷新Token", description = "为即将过期的Token生成新Token")
    @PostMapping ("/refresh-token")
    public Result<String> refreshToken(HttpServletRequest request){
        try {
            // 从请求头获取Token
            String oldToken = extractTokenFromRequest(request);

            if( oldToken==null ){
                return Result.error(401, "Token缺失，无法刷新");
            }

            // 刷新Token
            String newToken = userService.refreshToken(oldToken);

            if( newToken==null ){
                return Result.error(401, "Token无效，无法刷新，请重新登录");
            }

            return Result.success(newToken);

        } catch( Exception e ) {
            System.err.println("Token刷新失败: " + e.getMessage());
            return Result.error(500, "Token刷新失败，请重新登录");
        }
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 根据Token获取当前登录用户的基本信息
     * 用于前端获取用户状态和权限信息
     *
     * @param request HTTP请求，从Authorization头获取Token
     * @return 当前用户信息
     */
    @Operation (summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping ("/current-user")
    public Result<WechatLoginResponse.UserInfo> getCurrentUser(HttpServletRequest request){
        try {
            // 从请求头获取Token
            String token = extractTokenFromRequest(request);

            if( token==null ){
                return Result.error(401, "Token缺失，请先登录");
            }

            // 验证Token并获取用户信息
            WechatLoginResponse.UserInfo userInfo = userService.getCurrentUserByToken(token);

            if( userInfo==null ){
                return Result.error(401, "Token无效或用户不存在");
            }

            return Result.success(userInfo);

        } catch( Exception e ) {
            System.err.println("获取当前用户信息失败: " + e.getMessage());
            return Result.error(500, "获取用户信息失败");
        }
    }

    /**
     * 微信配置状态检查
     * <p>
     * 检查微信小程序配置是否正确
     * 用于系统健康检查和问题排查
     *
     * @return 配置状态信息
     */
    @Operation (summary = "微信配置状态检查", description = "检查微信小程序配置是否正确")
    @GetMapping ("/wechat-config-status")
    public Result<String> getWechatConfigStatus(){
        try {
            String status = wechatService.getConfigStatus();
            return Result.success(status);
        } catch( Exception e ) {
            return Result.error(500, "配置检查失败: " + e.getMessage());
        }
    }

    /**
     * 从HTTP请求中提取Token
     * <p>
     * 支持从Authorization头的Bearer Token中提取
     *
     * @param request HTTP请求
     * @return Token字符串，如果不存在返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request){
        // 从Authorization头获取Token
        String authHeader = request.getHeader("Authorization");
        if( authHeader!=null && authHeader.startsWith("Bearer ") ){
            return authHeader.substring(7); // 去掉"Bearer "前缀
        }

        // 也可以支持从参数中获取Token（可选）
        String tokenParam = request.getParameter("token");
        if( tokenParam!=null && !tokenParam.trim().isEmpty() ){
            return tokenParam;
        }

        return null;
    }
}