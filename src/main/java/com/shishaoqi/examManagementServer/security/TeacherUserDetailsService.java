package com.shishaoqi.examManagementServer.security;

import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeacherUserDetailsService implements UserDetailsService {

    private final TeacherService teacherService;

    public TeacherUserDetailsService(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Teacher teacher = teacherService.getTeacherByEmail(email);
        if (teacher == null) {
            throw new UsernameNotFoundException("未找到该邮箱对应的用户: " + email);
        }
        return new TeacherUserDetails(teacher);
    }
}