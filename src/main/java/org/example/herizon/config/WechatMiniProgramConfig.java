package org.example.herizon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置类
 * <p>
 * 从application.yaml中读取微信小程序相关配置
 * 使用@ConfigurationProperties自动绑定配置属性
 *
 * @author Kokoa
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniprogram")
public class WechatMiniProgramConfig {
    /**
     * 微信小程序AppID
     * <p>
     * 从微信公众平台获取，用于标识小程序身份
     */
    private String appId;

    /**
     * 微信小程序AppSecret
     * <p>
     * 从微信公众平台获取，用于调用微信API
     * 注意：这是敏感信息，生产环境应通过环境变量传入
     */
    private String appSecret;

    /**
     * 微信code2session接口地址
     * <p>
     * 用于将临时登录凭证code换取openid和session_key
     */
    private String code2sessionUrl;

    /**
     * 验证配置是否完整有效
     * <p>
     * 检查必要的配置项是否都已设置
     *
     * @return true=配置完整，false=配置不完整
     */
    public boolean isConfigValid() {
        return appId != null && !appId.trim().isEmpty() &&
               appSecret != null && !appSecret.trim().isEmpty() &&
               !appSecret.equals("your_miniprogram_secret_here") &&
               code2sessionUrl != null && !code2sessionUrl.trim().isEmpty();
    }

    /**
     * 构建code2session完整的请求URL
     * <p>
     * 根据配置参数和传入的code构建微信API调用URL
     *
     * @param code 微信登录临时凭证
     * @return 完整的API请求URL
     */
    public String buildCode2SessionUrl(String code) {
        if (!isConfigValid()) {
            throw new IllegalStateException("微信配置不完整，无法构建API URL");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("微信登录凭证code不能为空");
        }

        return String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                code2sessionUrl, appId, appSecret, code);
    }

    /**
     * 获取配置状态描述（用于健康检查和调试）
     * <p>
     * 返回当前配置的状态信息，不包含敏感数据
     *
     * @return 配置状态描述
     */
    public String getConfigStatus() {
        if (isConfigValid()) {
            return String.format("微信小程序配置正常 - AppID: %s, API地址: %s",
                    appId, code2sessionUrl);
        } else {
            StringBuilder status = new StringBuilder("微信小程序配置不完整:");
            if (appId == null || appId.trim().isEmpty()) {
                status.append(" [AppID缺失]");
            }
            if (appSecret == null || appSecret.trim().isEmpty() ||
                appSecret.equals("your_miniprogram_secret_here")) {
                status.append(" [AppSecret未配置]");
            }
            if (code2sessionUrl == null || code2sessionUrl.trim().isEmpty()) {
                status.append(" [API地址缺失]");
            }
            return status.toString();
        }
    }

    /**
     * 获取脱敏的AppSecret（用于日志记录）
     * <p>
     * 只显示前4位和后4位，中间用*代替
     *
     * @return 脱敏后的AppSecret
     */
    public String getMaskedAppSecret() {
        if (appSecret != null && appSecret.length() > 8) {
            return appSecret.substring(0, 4) + "****" + appSecret.substring(appSecret.length() - 4);
        }
        return "****";
    }
}