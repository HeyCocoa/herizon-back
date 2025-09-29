package org.example.herizon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置属性类
 * <p>
 * 从application.yaml中读取微信相关配置
 * 包括小程序的appid、secret和API地址等
 *
 * @author Kokoa
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniprogram")
public class WechatProperties {
    /**
     * 微信小程序appid
     */
    private String appid;

    /**
     * 微信小程序secret
     */
    private String secret;

    /**
     * 微信API配置
     */
    private ApiConfig api = new ApiConfig();

    @Data
    public static class ApiConfig {
        /**
         * 登录凭证校验API地址
         */
        private String code2session = "https://api.weixin.qq.com/sns/jscode2session";

        /**
         * 检验登录态API地址
         */
        private String checksession = "https://api.weixin.qq.com/wxa/checksession";
    }

    /**
     * 验证配置是否完整
     * @return true=配置完整，false=配置缺失
     */
    public boolean isConfigValid() {
        return appid != null && !appid.trim().isEmpty() &&
               secret != null && !secret.trim().isEmpty() &&
               !secret.equals("your_miniprogram_secret_here");
    }

    /**
     * 获取code2session完整URL
     * @param code 微信登录临时凭证
     * @return 完整的API调用URL
     */
    public String getCode2SessionUrl(String code) {
        return String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                api.getCode2session(), appid, secret, code);
    }
}