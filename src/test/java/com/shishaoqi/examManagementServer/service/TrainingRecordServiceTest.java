package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.repository.TrainingRecordMapper;
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
class TrainingRecordServiceTest {

    @Autowired
    private TrainingRecordService recordService;

    @MockBean
    private TrainingRecordMapper recordMapper;

    private TrainingRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new TrainingRecord();
        testRecord.setRecordId(1L);
        testRecord.setMaterialId(1L);
        testRecord.setTeacherId(1);
        testRecord.setStudyTime(60);
        testRecord.setExamScore(85);
        testRecord.setStatus(2);
        testRecord.setStartTime(LocalDateTime.now().minusHours(2));
        testRecord.setCompleteTime(LocalDateTime.now());
    }

    @Test
    void getTeacherRecords() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        when(recordMapper.selectList(any())).thenReturn(records);

        List<TrainingRecord> result = recordService.getTeacherRecords(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeacherId());
        assertEquals(85, result.get(0).getExamScore());
    }

    @Test
    void getMaterialRecords() {
        List<TrainingRecord> records = Arrays.asList(testRecord);
        when(recordMapper.selectList(any())).thenReturn(records);

        List<TrainingRecord> result = recordService.getMaterialRecords(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getMaterialId());
        assertEquals(60, result.get(0).getStudyTime());
    }
}