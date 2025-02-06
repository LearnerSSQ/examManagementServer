package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    }

    @Test
    void getTeachersByDepartment() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.selectList(any())).thenReturn(teachers);

        List<Teacher> result = teacherService.getTeachersByDepartment("计算机系");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
        assertEquals("计算机系", result.get(0).getDepartment());
    }

    @Test
    void getTeacherByEmail() {
        when(teacherMapper.selectOne(any())).thenReturn(testTeacher);

        Teacher result = teacherService.getTeacherByEmail("zhangsan@example.com");

        assertNotNull(result);
        assertEquals("zhangsan@example.com", result.getEmail());
        assertEquals("张三", result.getName());
    }

    @Test
    void getTeacherByPhone() {
        when(teacherMapper.selectOne(any())).thenReturn(testTeacher);

        Teacher result = teacherService.getTeacherByPhone("13800138000");

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
        assertEquals("张三", result.getName());
    }
}