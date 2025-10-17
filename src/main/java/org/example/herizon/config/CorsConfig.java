package org.example.herizon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置类
 * 用于解决前后端分离开发时的跨域访问问题
 *
 * @author Claude
 * @date 2025-09-30
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器，允许前端跨域请求后端API
     *
     * 配置说明：
     * - 允许所有来源（开发环境）- 生产环境应限制为具体域名
     * - 允许所有HTTP方法（GET, POST, PUT, DELETE等）
     * - 允许所有请求头
     * - 允许携带认证信息（Cookie、Token等）
     * - 预检请求缓存时间3600秒
     *
     * @return CorsFilter CORS过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源访问（开发环境配置）codex
        // 生产环境应改为: config.addAllowedOrigin("https://yourdomain.com");
        config.addAllowedOriginPattern("*");

        // 允许携带认证信息（Cookie、Authorization header等）
        config.setAllowCredentials(true);

        // 允许所有HTTP方法（GET, POST, PUT, DELETE, OPTIONS等）
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 暴露的响应头，前端可以访问这些响应头
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Authorization");

        // 预检请求的有效期，单位为秒（1小时）
        // 在此时间内，不需要再发送预检请求
        config.setMaxAge(3600L);

        // 创建CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // server.servlet.context-path=/api 已经作为统一前缀
        // 这里使用 /** 确保剥离上下文路径后仍能匹配到所有控制器
        source.registerCorsConfiguration("/**", config);

        // 返回配置好的CORS过滤器
        return new CorsFilter(source);
    }
}


