package com.shishaoqi.examManagementServer.security.annotation;

import java.lang.annotation.*;

import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /**
     * 允许访问的角色列表
     */
    TeacherRole[] value();

    /**
     * 是否允许系统管理员访问
     * 默认为true，表示系统管理员可以访问任何受保护的资源
     */
    boolean allowAdmin() default true;

    /**
     * 错误消息
     */
    String message() default "无权限执行此操作";
}