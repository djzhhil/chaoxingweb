package com.chaoxingweb.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置类
 * 用于生成 API 文档
 *
 * @author 小克
 * @since 2026-03-29
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("超星学习通自动化工具 API")
                        .description("超星学习通自动化工具的 RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("小克")
                                .email("xiaoke@example.com")
                                .url("https://github.com/djzhhil/chaoxingweb"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // JWT 认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
