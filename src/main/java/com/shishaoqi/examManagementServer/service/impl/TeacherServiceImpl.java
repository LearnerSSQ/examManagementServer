package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Override
    public List<Teacher> getTeachersByDepartment(String department) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getDepartment, department);
        return list(wrapper);
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getEmail, email);
        return getOne(wrapper);
    }

    @Override
    public Teacher getTeacherByPhone(String phone) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getPhone, phone);
        return getOne(wrapper);
    }
}