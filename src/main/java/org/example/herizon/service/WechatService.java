package org.example.herizon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.herizon.config.WechatMiniProgramConfig;
import org.example.herizon.dto.Code2SessionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信服务类
 * <p>
 * 处理与微信API的交互，包括：
 * - 登录凭证校验（code2session）
 * - 会话密钥验证（checksession）
 * - 微信用户数据解密等功能
 *
 * @author Kokoa
 */
@Service
public class WechatService {

    @Autowired
    private WechatMiniProgramConfig wechatConfig;

    /**
     * HTTP客户端，用于调用微信API
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * JSON解析器
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证微信登录凭证
     * <p>
     * 调用微信code2session接口，将临时登录凭证code换取openid和session_key
     * 这是微信登录流程的核心步骤
     *
     * @param code 微信登录临时凭证（通过wx.login获取）
     * @return 微信API响应，包含openid、session_key、unionid等信息
     * @throws RuntimeException 当微信API调用失败时抛出异常
     */
    public Code2SessionResponse code2Session(String code) {
        // 验证配置是否完整
        if (!wechatConfig.isConfigValid()) {
            throw new RuntimeException("微信配置不完整：" + wechatConfig.getConfigStatus());
        }

        // 验证code参数
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("微信登录凭证code不能为空");
        }

        String responseBody = null;
        try {
            // 构建API请求URL
            String url = wechatConfig.buildCode2SessionUrl(code);

            // 调用微信API
            responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("微信API返回数据为空");
            }

            // 解析JSON响应
            Code2SessionResponse response = objectMapper.readValue(responseBody, Code2SessionResponse.class);

            // 检查API调用是否成功
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getFormattedErrorMessage());
            }

            // 验证返回数据的完整性
            if (!response.isDataComplete()) {
                throw new RuntimeException("微信API返回数据不完整：openid或session_key为空");
            }

            return response;

        } catch (JsonProcessingException e) {
            // JSON解析异常
            System.err.println("微信API响应JSON解析失败: " + e.getMessage());
            System.err.println("原始响应内容: " + (responseBody != null ? responseBody : "null"));
            throw new RuntimeException("微信API响应格式错误，请检查微信配置", e);
        } catch (RuntimeException e) {
            // 业务逻辑异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常
            System.err.println("微信code2session调用失败: " + e.getMessage());
            throw new RuntimeException("微信登录服务暂时不可用，请稍后重试", e);
        }
    }

    /**
     * 验证微信会话是否有效
     * <p>
     * 调用微信checksession接口，检查session_key是否过期
     * 注意：此功能需要access_token，实际使用中可能需要额外的认证流程
     *
     * @param openid 微信用户openid
     * @param sessionKey 会话密钥
     * @return true=会话有效，false=会话已过期
     */
    public boolean checkSession(String openid, String sessionKey) {
        // 此方法需要access_token，在实际项目中需要实现获取access_token的逻辑
        // 暂时返回true，表示会话有效
        // TODO: 实现真实的会话检查逻辑
        return true;
    }

    /**
     * 解密微信用户数据
     * <p>
     * 使用session_key解密微信返回的加密用户数据
     * 包括用户手机号、详细地址等敏感信息
     *
     * @param encryptedData 加密的用户数据
     * @param iv 初始向量
     * @param sessionKey 会话密钥
     * @return 解密后的用户数据JSON字符串
     */
    public String decryptUserData(String encryptedData, String iv, String sessionKey) {
        // TODO: 实现微信数据解密算法
        // 微信使用AES-128-CBC算法进行数据加密
        // 需要使用session_key作为密钥，iv作为初始向量进行解密
        throw new RuntimeException("微信数据解密功能暂未实现");
    }

    /**
     * 生成微信登录二维码
     * <p>
     * 用于网页版微信登录，生成二维码供用户扫描
     * 当前主要针对小程序，此功能可选实现
     *
     * @return 二维码图片URL或Base64数据
     */
    public String generateQrCode() {
        // TODO: 实现二维码生成逻辑
        throw new RuntimeException("微信二维码登录功能暂未实现");
    }

    /**
     * 获取微信配置信息（用于前端调试）
     * <p>
     * 返回当前微信配置状态，不包含敏感信息
     *
     * @return 配置状态信息
     */
    public String getConfigStatus() {
        return wechatConfig.getConfigStatus();
    }
}