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
     * 根据邮箱获取教师信息
     */
    Teacher getTeacherByEmail(String email);

    /**
     * 根据手机号获取教师信息
     */
    Teacher getTeacherByPhone(String phone);

    /**
     * 更新教师状态
     */
    boolean updateStatus(Integer teacherId, Integer status);

    /**
     * 更新最后登录时间
     */
    boolean updateLastLogin(Integer teacherId);
}