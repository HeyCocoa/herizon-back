package org.example.herizon.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 * <p>
 * 封装用户登录时需要提供的凭证信息
 * 支持使用用户名或邮箱作为登录标识
 *
 * @author Kokoa
 */
@Data
public class UserLoginRequest {
    /**
     * 登录标识
     * <p>
     * 可以是用户名或邮箱地址
     * 系统会自动识别输入类型
     */
    private String identifier;

    /**
     * 用户密码
     */
    private String password;
}