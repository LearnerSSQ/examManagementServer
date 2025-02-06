package com.shishaoqi.examManagementServer.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI examManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("考试管理系统API文档")
                        .description("提供考试管理系统的所有API接口说明")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ShiShaoqi")
                                .email("")
                                .url("")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }
}