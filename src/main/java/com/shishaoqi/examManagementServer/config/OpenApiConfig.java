package com.shishaoqi.examManagementServer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI examManagementApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("考试管理系统 API")
                        .description("考试管理系统的 RESTful API 文档")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}