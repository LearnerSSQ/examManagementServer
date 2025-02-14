package com.shishaoqi.examManagementServer.security.aspect;

import com.shishaoqi.examManagementServer.security.annotation.RequireRole;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.security.TeacherUserDetails;

import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("未登录");
        }

        TeacherUserDetails userDetails = (TeacherUserDetails) authentication.getPrincipal();
        Teacher teacher = userDetails.getTeacher();

        if (teacher == null || teacher.getRole() == null) {
            throw new AccessDeniedException("用户角色未定义");
        }

        // 如果是系统管理员且允许管理员访问，直接通过
        if (teacher.getRole() == TeacherRole.ADMIN && requireRole.allowAdmin()) {
            return;
        }

        // 检查用户是否具有所需角色
        boolean hasRole = Arrays.asList(requireRole.value()).contains(teacher.getRole());
        if (!hasRole) {
            throw new AccessDeniedException(requireRole.message());
        }
    }
}