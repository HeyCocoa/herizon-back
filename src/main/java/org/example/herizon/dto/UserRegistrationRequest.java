package org.example.herizon.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 * <p>
 * 封装用户注册时需要提供的信息，包括：
 * - 基本账户信息（用户名、邮箱、密码）
 * - 身份认证问卷数据
 *
 * @author Kokoa
 */
@Data
public class UserRegistrationRequest {
    /**
     * 用户名，唯一标识
     * 要求：3-20个字符，支持字母、数字、下划线
     */
    private String username;

    /**
     * 邮箱地址，用于登录和联系
     * 要求：有效的邮箱格式
     */
    private String email;

    /**
     * 密码
     * 要求：6-20个字符，包含字母和数字
     */
    private String password;

    /**
     * 身份认证问卷数据(JSON格式)
     * <p>
     * 用于验证用户的女性身份和职场背景，包含：
     * - 性别确认
     * - 职业信息
     * - 教育背景
     * - 身份验证相关问题
     */
    private String questionnaireData;
}