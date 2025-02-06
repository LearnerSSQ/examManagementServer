package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import com.shishaoqi.examManagementServer.repository.InvigilationRecordMapper;
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
class InvigilationRecordServiceTest {

    @Autowired
    private InvigilationRecordService recordService;

    @MockBean
    private InvigilationRecordMapper recordMapper;

    private InvigilationRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new InvigilationRecord();
        testRecord.setRecordId(1L);
        testRecord.setAssignmentId(1L);
        testRecord.setType(1);
        testRecord.setDescription("已到达考场");
        testRecord.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getRecordsByAssignment() {
        List<InvigilationRecord> records = Arrays.asList(testRecord);
        when(recordMapper.getRecordsByAssignment(1L)).thenReturn(records);

        List<InvigilationRecord> result = recordService.getRecordsByAssignment(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAssignmentId());
        assertEquals("已到达考场", result.get(0).getDescription());
    }

    @Test
    void getSignInRecord() {
        when(recordMapper.getSignInRecord(1L)).thenReturn(testRecord);

        InvigilationRecord result = recordService.getSignInRecord(1L);

        assertNotNull(result);
        assertEquals(1, result.getType());
        assertEquals("已到达考场", result.getDescription());
    }

    @Test
    void getExceptionRecords() {
        testRecord.setType(2);
        testRecord.setDescription("考生迟到5分钟");
        List<InvigilationRecord> records = Arrays.asList(testRecord);
        when(recordMapper.selectList(any())).thenReturn(records);

        List<InvigilationRecord> result = recordService.getExceptionRecords(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getType());
        assertEquals("考生迟到5分钟", result.get(0).getDescription());
    }

    @Test
    void hasSignedIn() {
        when(recordMapper.getSignInRecord(1L)).thenReturn(testRecord);

        boolean result = recordService.hasSignedIn(1L);

        assertTrue(result);
    }

    @Test
    void countExceptionRecords() {
        when(recordMapper.selectCount(any())).thenReturn(2L);

        int result = recordService.countExceptionRecords(1L);

        assertEquals(2, result);
    }
}