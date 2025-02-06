package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class TeacherServiceTest {

    @Autowired
    private TeacherService teacherService;

    @MockBean
    private TeacherMapper teacherMapper;

    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        testTeacher = new Teacher();
        testTeacher.setTeacherId(1);
        testTeacher.setName("张三");
        testTeacher.setEmail("zhangsan@example.com");
        testTeacher.setPhone("13800138000");
        testTeacher.setDepartment("计算机系");
        testTeacher.setTitle("副教授");
        testTeacher.setStatus(1);
        testTeacher.setLastLogin(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据部门查询教师列表 - 成功场景")
    void getTeachersByDepartment_Success() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.selectList(any())).thenReturn(teachers);

        List<Teacher> result = teacherService.getTeachersByDepartment("计算机系");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
        assertEquals("计算机系", result.get(0).getDepartment());
        verify(teacherMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("根据部门查询教师列表 - 部门为空")
    void getTeachersByDepartment_EmptyDepartment() {
        assertThrows(BusinessException.class, () -> teacherService.getTeachersByDepartment(null));
        assertThrows(BusinessException.class, () -> teacherService.getTeachersByDepartment(""));
        verify(teacherMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("根据部门查询教师列表 - 部门不存在")
    void getTeachersByDepartment_DepartmentNotFound() {
        when(teacherMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Teacher> result = teacherService.getTeachersByDepartment("不存在的部门");

        assertTrue(result.isEmpty());
        verify(teacherMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("根据邮箱查询教师 - 成功场景")
    void getTeacherByEmail_Success() {
        when(teacherMapper.selectOne(any())).thenReturn(testTeacher);

        Teacher result = teacherService.getTeacherByEmail("zhangsan@example.com");

        assertNotNull(result);
        assertEquals("zhangsan@example.com", result.getEmail());
        assertEquals("张三", result.getName());
        verify(teacherMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("根据邮箱查询教师 - 邮箱为空")
    void getTeacherByEmail_EmptyEmail() {
        assertThrows(BusinessException.class, () -> teacherService.getTeacherByEmail(null));
        assertThrows(BusinessException.class, () -> teacherService.getTeacherByEmail(""));
        verify(teacherMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("根据邮箱查询教师 - 教师不存在")
    void getTeacherByEmail_TeacherNotFound() {
        when(teacherMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> teacherService.getTeacherByEmail("notexist@example.com"));
        verify(teacherMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("根据手机号查询教师 - 成功场景")
    void getTeacherByPhone_Success() {
        when(teacherMapper.selectOne(any())).thenReturn(testTeacher);

        Teacher result = teacherService.getTeacherByPhone("13800138000");

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
        assertEquals("张三", result.getName());
        verify(teacherMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("根据手机号查询教师 - 手机号为空")
    void getTeacherByPhone_EmptyPhone() {
        assertThrows(BusinessException.class, () -> teacherService.getTeacherByPhone(null));
        assertThrows(BusinessException.class, () -> teacherService.getTeacherByPhone(""));
        verify(teacherMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("根据手机号查询教师 - 教师不存在")
    void getTeacherByPhone_TeacherNotFound() {
        when(teacherMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> teacherService.getTeacherByPhone("13900139000"));
        verify(teacherMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("更新教师状态 - 成功场景")
    void updateStatus_Success() {
        when(teacherMapper.selectById(1)).thenReturn(testTeacher);
        when(teacherMapper.updateStatus(1, 2)).thenReturn(1);

        boolean result = teacherService.updateStatus(1, 2);

        assertTrue(result);
        verify(teacherMapper, times(1)).selectById(1);
        verify(teacherMapper, times(1)).updateStatus(1, 2);
    }

    @Test
    @DisplayName("更新教师状态 - 参数无效")
    void updateStatus_InvalidParams() {
        assertThrows(BusinessException.class, () -> teacherService.updateStatus(null, 1));
        assertThrows(BusinessException.class, () -> teacherService.updateStatus(1, null));
        assertThrows(BusinessException.class, () -> teacherService.updateStatus(1, 3));
        verify(teacherMapper, never()).updateStatus(anyInt(), anyInt());
    }

    @Test
    @DisplayName("更新最后登录时间 - 成功场景")
    void updateLastLogin_Success() {
        when(teacherMapper.selectById(1)).thenReturn(testTeacher);
        when(teacherMapper.updateLastLogin(eq(1), any(LocalDateTime.class))).thenReturn(1);

        boolean result = teacherService.updateLastLogin(1);

        assertTrue(result);
        verify(teacherMapper, times(1)).selectById(1);
        verify(teacherMapper, times(1)).updateLastLogin(eq(1), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("更新最后登录时间 - 教师已禁用")
    void updateLastLogin_TeacherDisabled() {
        testTeacher.setStatus(2);
        when(teacherMapper.selectById(1)).thenReturn(testTeacher);

        assertThrows(BusinessException.class, () -> teacherService.updateLastLogin(1));
        verify(teacherMapper, times(1)).selectById(1);
        verify(teacherMapper, never()).updateLastLogin(anyInt(), any(LocalDateTime.class));
    }
}