package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table ("users")
public class User{
    @Id (keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户名（可选，微信登录用户可能没有）
     */
    private String username;

    /**
     * 邮箱（可选，微信登录用户可能没有）
     */
    private String email;

    /**
     * 密码哈希（可选，微信登录用户没有密码）
     */
    private String passwordHash;

    /**
     * 用户角色: 0=体验用户, 1=正式用户, 2=管理员
     */
    private Integer role;

    /**
     * 身份认证问卷数据(JSON格式)
     */
    private String questionnaireData;

    /**
     * 微信相关字段
     */

    /**
     * 微信用户唯一标识（openid）
     * 每个用户在不同小程序下的openid是不同的
     */
    private String wechatOpenid;

    /**
     * 微信开放平台统一标识（unionid）
     * 同一用户在不同应用下的unionid是相同的，可选字段
     */
    private String wechatUnionid;

    /**
     * 微信会话密钥（session_key）
     * 用于解密微信数据，需要定期更新
     */
    private String wechatSessionKey;

    /**
     * 用户昵称（优先使用微信昵称）
     */
    private String nickname;

    /**
     * 用户头像URL（优先使用微信头像）
     */
    private String avatar;

    /**
     * 用户注册来源: 1=普通注册, 2=微信小程序, 3=微信App, 4=其他第三方
     */
    private Integer registerSource;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}