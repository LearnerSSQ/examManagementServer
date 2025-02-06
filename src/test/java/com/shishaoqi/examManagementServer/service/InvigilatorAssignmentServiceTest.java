package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class InvigilatorAssignmentServiceTest {

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @MockBean
    private InvigilatorAssignmentMapper assignmentMapper;

    private InvigilatorAssignment testAssignment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testAssignment = new InvigilatorAssignment();
        testAssignment.setAssignmentId(1L);
        testAssignment.setTeacherId(1);
        testAssignment.setCourseName("高等数学");
        testAssignment.setExamStart(now.plusDays(1));
        testAssignment.setExamEnd(now.plusDays(1).plusHours(2));
        testAssignment.setLocation("教学楼A-101");
        testAssignment.setRole(0);
        testAssignment.setStatus(1);
    }

    @Test
    void getTeacherAssignments() {
        List<InvigilatorAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentMapper.selectList(any())).thenReturn(assignments);

        List<InvigilatorAssignment> result = assignmentService.getTeacherAssignments(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("高等数学", result.get(0).getCourseName());
        assertEquals(1, result.get(0).getTeacherId());
    }

    @Test
    void getAssignmentsByTimeRange() {
        List<InvigilatorAssignment> assignments = Arrays.asList(testAssignment);
        when(assignmentMapper.selectList(any())).thenReturn(assignments);

        List<InvigilatorAssignment> result = assignmentService.getAssignmentsByTimeRange(
                now, now.plusDays(2));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(result.get(0).getExamStart().isAfter(now));
        assertTrue(result.get(0).getExamEnd().isBefore(now.plusDays(2)));
    }
}