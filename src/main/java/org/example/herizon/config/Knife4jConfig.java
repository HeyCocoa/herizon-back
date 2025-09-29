package org.example.herizon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 * <p>
 * 配置项目的API文档信息，包括标题、描述、版本等
 *
 * @author Kokoa
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Herizon女性社区API文档")
                        .description("Herizon女性职业赋能社区平台的后端API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Herizon开发团队")
                                .email("dev@herizon.com")));
    }
}