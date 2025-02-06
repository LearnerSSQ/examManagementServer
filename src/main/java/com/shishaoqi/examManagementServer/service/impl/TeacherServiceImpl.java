package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);

    @Override
    public List<Teacher> getTeachersByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            log.error("获取部门教师列表失败：部门名称为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("获取部门教师列表，部门：{}", department);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getDepartment, department)
                .orderByAsc(Teacher::getTeacherId);
        List<Teacher> teachers = list(wrapper);
        log.info("获取到{}名教师，部门：{}", teachers.size(), department);
        return teachers;
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("根据邮箱查询教师失败：邮箱为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("根据邮箱查询教师，邮箱：{}", email);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getEmail, email);
        Teacher teacher = getOne(wrapper);
        if (teacher == null) {
            log.warn("未找到教师，邮箱：{}", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return teacher;
    }

    @Override
    public Teacher getTeacherByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            log.error("根据手机号查询教师失败：手机号为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("根据手机号查询教师，手机号：{}", phone);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getPhone, phone);
        Teacher teacher = getOne(wrapper);
        if (teacher == null) {
            log.warn("未找到教师，手机号：{}", phone);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return teacher;
    }

    @Override
    public boolean updateStatus(Integer teacherId, Integer status) {
        if (teacherId == null) {
            log.error("更新教师状态失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (status == null || status < 0 || status > 2) {
            log.error("更新教师状态失败：无效的状态值 {}", status);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.error("更新教师状态失败：教师不存在，ID={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (status == 2) {
            log.warn("教师账号将被禁用，ID={}", teacherId);
        }

        boolean success = baseMapper.updateStatus(teacherId, status) > 0;
        if (success) {
            log.info("成功更新教师状态，教师ID：{}，新状态：{}", teacherId, status);
        } else {
            log.error("更新教师状态失败，教师ID：{}", teacherId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    public boolean updateLastLogin(Integer teacherId) {
        if (teacherId == null) {
            log.error("更新最后登录时间失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.error("更新最后登录时间失败：教师不存在，ID={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (teacher.getStatus() == 2) {
            log.error("更新最后登录时间失败：教师账号已禁用，ID={}", teacherId);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        boolean success = baseMapper.updateLastLogin(teacherId, LocalDateTime.now()) > 0;
        if (success) {
            log.info("成功更新教师最后登录时间，教师ID：{}", teacherId);
        } else {
            log.error("更新教师最后登录时间失败，教师ID：{}", teacherId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }
}