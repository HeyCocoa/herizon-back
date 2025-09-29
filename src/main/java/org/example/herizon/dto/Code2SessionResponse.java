package org.example.herizon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信code2Session接口响应DTO
 * <p>
 * 封装微信API返回的用户身份信息
 * 对应微信官方文档中的code2session接口响应格式
 *
 * @author Kokoa
 */
@Data
public class Code2SessionResponse {
    /**
     * 微信用户唯一标识
     * <p>
     * 用户在当前小程序下的唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     * <p>
     * 用于解密微信用户敏感数据的密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 微信开放平台统一标识（可选）
     * <p>
     * 只有当小程序绑定到微信开放平台账号时才返回
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
     * 当errcode不为0时，包含具体的错误描述
     */
    private String errmsg;

    /**
     * 检查API调用是否成功
     * <p>
     * 根据errcode判断调用结果
     *
     * @return true=成功，false=失败
     */
    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }

    /**
     * 获取格式化的错误信息
     * <p>
     * 用于日志记录和错误提示
     *
     * @return 格式化的错误信息
     */
    public String getFormattedErrorMessage() {
        if (isSuccess()) {
            return "API调用成功";
        }
        return String.format("微信API调用失败 [错误码:%d] %s", errcode, errmsg != null ? errmsg : "未知错误");
    }

    /**
     * 验证响应数据的完整性
     * <p>
     * 检查必要字段是否存在
     *
     * @return true=数据完整，false=数据不完整
     */
    public boolean isDataComplete() {
        return isSuccess() &&
               openid != null && !openid.trim().isEmpty() &&
               sessionKey != null && !sessionKey.trim().isEmpty();
    }
}