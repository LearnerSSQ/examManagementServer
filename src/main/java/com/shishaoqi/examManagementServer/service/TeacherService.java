package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Teacher;
import java.util.List;

public interface TeacherService extends IService<Teacher> {

    /**
     * 根据部门获取教师列表
     */
    List<Teacher> getTeachersByDepartment(String department);

    /**
     * 根据邮箱获取教师
     */
    Teacher getTeacherByEmail(String email);

    /**
     * 根据手机号获取教师
     */
    Teacher getTeacherByPhone(String phone);
}