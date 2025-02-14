package com.shishaoqi.examManagementServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // 系统管理相关重定向
        registry.addRedirectViewController("/system/settings", "/api/admin/settings");
        registry.addRedirectViewController("/system/logs", "/api/admin/logs");

        // 教师管理重定向
        registry.addRedirectViewController("/api/teachers/manage", "/api/admin/teachers");
    }
}