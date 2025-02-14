package com.shishaoqi.examManagementServer.security;

import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeacherUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(TeacherUserDetailsService.class);
    private final TeacherService teacherService;

    public TeacherUserDetailsService(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("正在加载用户信息，邮箱：{}", email);

        try {
            Teacher teacher = teacherService.getTeacherByEmail(email);

            if (teacher == null) {
                log.warn("未找到该邮箱对应的用户: {}", email);
                throw new UsernameNotFoundException("未找到该邮箱对应的用户: " + email);
            }

            if (teacher.getStatus() == TeacherStatus.DISABLED) {
                log.warn("用户账号已被禁用，邮箱：{}", email);
                throw new UsernameNotFoundException("账号已被禁用");
            }

            if (teacher.getStatus() == TeacherStatus.INACTIVE) {
                log.warn("用户账号未激活，邮箱：{}", email);
                throw new UsernameNotFoundException("账号未激活");
            }

            log.debug("成功加载用户信息，用户：{}，角色：{}", teacher.getName(), teacher.getRole());
            return new TeacherUserDetails(teacher);
        } catch (Exception e) {
            log.error("加载用户信息时发生错误，邮箱：{}", email, e);
            throw new UsernameNotFoundException("加载用户信息失败: " + e.getMessage());
        }
    }
}