package org.example.herizon.dto;

import lombok.Data;

/**
 * 微信登录请求DTO
 * <p>
 * 封装微信小程序登录时提供的数据
 * 包含微信授权码和用户基本信息
 *
 * @author Kokoa
 */
@Data
public class WechatLoginRequest {
    /**
     * 微信登录临时凭证code
     * <p>
     * 通过wx.login()获取的临时登录凭证
     * 有效期5分钟，用于换取openid和session_key
     */
    private String code;

    /**
     * 微信用户昵称（可选）
     * <p>
     * 如果用户授权获取用户信息，则包含此字段
     */
    private String nickname;

    /**
     * 微信用户头像URL（可选）
     * <p>
     * 如果用户授权获取用户信息，则包含此字段
     */
    private String avatar;

    /**
     * 用户注册来源标识
     * <p>
     * 2=微信小程序, 3=微信App, 默认为微信小程序
     */
    private Integer registerSource = 2;

    /**
     * 额外的身份认证问卷数据（可选）
     * <p>
     * 如果是新用户注册，可以包含身份认证问卷
     * JSON格式存储
     */
    private String questionnaireData;
}