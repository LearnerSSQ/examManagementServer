package com.shishaoqi.examManagementServer.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class TeacherUserDetails implements UserDetails {

    private final Teacher teacher;

    public TeacherUserDetails(Teacher teacher) {
        this.teacher = teacher;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加基本角色
        authorities.add(new SimpleGrantedAuthority("ROLE_" + teacher.getRole().name()));

        // 如果是管理员，添加额外权限
        if (teacher.getRole() == TeacherRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_TEACHERS"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SYSTEM"));
        }

        // 如果是考务管理员，添加考务管理权限
        if (teacher.getRole() == TeacherRole.EXAM_ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_EXAMS"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return teacher.getPassword();
    }

    @Override
    public String getUsername() {
        return teacher.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return teacher.getStatus() == TeacherStatus.ACTIVE;
    }

    public Teacher getTeacher() {
        return teacher;
    }
}