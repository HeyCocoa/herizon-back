package org.example.herizon.dto;

import lombok.Data;

/**
 * 微信API响应DTO
 * <p>
 * 封装微信code2session接口的响应数据
 * 用于接收openid、session_key、unionid等信息
 *
 * @author Kokoa
 */
@Data
public class WechatApiResponse {
    /**
     * 微信用户唯一标识
     * <p>
     * 每个用户在每个小程序下都有唯一的openid
     */
    private String openid;

    /**
     * 会话密钥
     * <p>
     * 用于解密微信用户数据，需要妥善保存
     */
    private String session_key;

    /**
     * 微信开放平台统一标识（可选）
     * <p>
     * 只有当小程序绑定到微信开放平台时才会返回
     */
    private String unionid;

    /**
     * 错误码
     * <p>
     * 0表示成功，其他值表示错误
     */
    private Integer errcode;

    /**
     * 错误信息
     * <p>
     * 当errcode不为0时，包含错误描述
     */
    private String errmsg;

    /**
     * 检查响应是否成功
     * @return true=成功，false=失败
     */
    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }

    /**
     * 获取错误信息描述
     * @return 错误信息，成功时返回"success"
     */
    public String getErrorMessage() {
        if (isSuccess()) {
            return "success";
        }
        return String.format("微信API调用失败 [%d]: %s", errcode, errmsg);
    }
}