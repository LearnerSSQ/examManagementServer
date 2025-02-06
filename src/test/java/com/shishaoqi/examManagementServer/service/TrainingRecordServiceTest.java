package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.repository.TrainingRecordMapper;
import com.shishaoqi.examManagementServer.service.impl.TrainingRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TrainingRecordServiceTest {

    @Mock
    private TrainingRecordMapper recordMapper;

    @Mock
    private TrainingMaterialMapper materialMapper;

    private TrainingRecordService trainingRecordService;
    private TrainingMaterial testMaterial;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingRecordService = new TrainingRecordServiceImpl(materialMapper);

        // 初始化测试用培训材料
        testMaterial = new TrainingMaterial();
        testMaterial.setMaterialId(1L);
        testMaterial.setTitle("测试培训材料");
        testMaterial.setRequiredTime(100);
        testMaterial.setValidityPeriod(6);
    }

    @Test
    @DisplayName("创建培训记录 - 成功场景")
    void createRecord_Success() {
        // 准备测试数据
        Integer teacherId = 1;
        Long materialId = 1L;
        when(materialMapper.selectById(materialId)).thenReturn(testMaterial);
        when(recordMapper.insert(any(TrainingRecord.class))).thenReturn(1);

        // 执行测试
        TrainingRecord result = trainingRecordService.createRecord(teacherId, materialId);

        // 验证结果
        assertNotNull(result);
        assertEquals(teacherId, result.getTeacherId());
        assertEquals(materialId, result.getMaterialId());
        assertEquals(0, result.getStudyTime());
        assertEquals(0, result.getStatus());
        verify(materialMapper, times(1)).selectById(materialId);
        verify(recordMapper, times(1)).insert(any(TrainingRecord.class));
    }

    @Test
    @DisplayName("创建培训记录 - 参数无效")
    void createRecord_InvalidParams() {
        assertThrows(BusinessException.class, () -> trainingRecordService.createRecord(null, 1L));
        assertThrows(BusinessException.class, () -> trainingRecordService.createRecord(1, null));
        verify(recordMapper, never()).insert(any());
    }

    @Test
    @DisplayName("更新学习进度 - 成功场景")
    void updateStudyProgress_Success() {
        // 准备测试数据
        Long recordId = 1L;
        Integer studyTime = 30;
        TrainingRecord record = createTestRecord(recordId, 0, null, 60);
        when(recordMapper.selectById(recordId)).thenReturn(record);
        when(recordMapper.updateById(any(TrainingRecord.class))).thenReturn(1);

        // 执行测试
        boolean result = trainingRecordService.updateStudyProgress(recordId, studyTime);

        // 验证结果
        assertTrue(result);
        assertEquals(90, record.getStudyTime());
        verify(recordMapper, times(1)).selectById(recordId);
        verify(recordMapper, times(1)).updateById(record);
    }

    @Test
    @DisplayName("更新学习进度 - 记录不存在")
    void updateStudyProgress_RecordNotFound() {
        when(recordMapper.selectById(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> trainingRecordService.updateStudyProgress(1L, 30));
        verify(recordMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("验证学习时长 - 满足要求")
    void validateStudyTime_MeetRequirement() {
        // 准备测试数据
        Long recordId = 1L;
        TrainingRecord record = createTestRecord(recordId, 1, null, 120);
        record.setMaterialId(1L);

        when(recordMapper.selectById(recordId)).thenReturn(record);
        when(materialMapper.selectById(record.getMaterialId())).thenReturn(testMaterial);

        // 执行测试
        boolean result = trainingRecordService.validateStudyTime(recordId);

        // 验证结果
        assertTrue(result);
        verify(recordMapper, times(1)).selectById(recordId);
        verify(materialMapper, times(1)).selectById(record.getMaterialId());
    }

    @Test
    @DisplayName("验证学习时长 - 不满足要求")
    void validateStudyTime_NotMeetRequirement() {
        // 准备测试数据
        Long recordId = 1L;
        TrainingRecord record = createTestRecord(recordId, 0, null, 50);
        record.setMaterialId(1L);

        when(recordMapper.selectById(recordId)).thenReturn(record);
        when(materialMapper.selectById(record.getMaterialId())).thenReturn(testMaterial);

        // 执行测试
        boolean result = trainingRecordService.validateStudyTime(recordId);

        // 验证结果
        assertFalse(result);
    }

    @Test
    @DisplayName("获取培训统计信息 - 成功场景")
    void getTrainingStatistics_Success() {
        // 准备测试数据
        Integer teacherId = 1;
        List<TrainingRecord> records = Arrays.asList(
                createTestRecord(1L, 2, 90, 120),
                createTestRecord(2L, 2, 85, 90),
                createTestRecord(3L, 1, null, 60));

        when(recordMapper.selectList(any())).thenReturn(records);

        // 执行测试
        Map<String, Object> stats = trainingRecordService.getTrainingStatistics(teacherId);

        // 验证结果
        assertNotNull(stats);
        assertEquals(66.67, (double) stats.get("completionRate"), 0.01);
        assertEquals(87.5, (double) stats.get("averageScore"), 0.01);
        assertEquals(270, (int) stats.get("totalStudyTime"));
        assertEquals(2L, (long) stats.get("completedCount"));
        verify(recordMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("获取未完成培训 - 成功场景")
    void getUnfinishedTrainings_Success() {
        // 准备测试数据
        Integer teacherId = 1;
        List<TrainingRecord> records = Arrays.asList(
                createTestRecord(1L, 0, null, 30),
                createTestRecord(2L, 1, null, 60));

        when(recordMapper.selectList(any())).thenReturn(records);

        // 执行测试
        List<TrainingRecord> result = trainingRecordService.getUnfinishedTrainings(teacherId);

        // 验证结果
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(r -> r.getStatus() == 2));
        verify(recordMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("检查培训是否过期 - 已过期")
    void isTrainingExpired_Expired() {
        // 准备测试数据
        Integer teacherId = 1;
        Long materialId = 1L;
        TrainingRecord record = new TrainingRecord();
        record.setCompleteTime(LocalDateTime.now().minusMonths(7));

        when(recordMapper.selectOne(any())).thenReturn(record);
        when(materialMapper.selectById(materialId)).thenReturn(testMaterial);

        // 执行测试
        boolean result = trainingRecordService.isTrainingExpired(teacherId, materialId);

        // 验证结果
        assertTrue(result);
        verify(recordMapper, times(1)).selectOne(any());
        verify(materialMapper, times(1)).selectById(materialId);
    }

    @Test
    @DisplayName("批量检查培训状态 - 成功场景")
    void batchCheckTrainingStatus_Success() {
        // 准备测试数据
        List<Integer> teacherIds = Arrays.asList(1, 2, 3);
        Long materialId = 1L;
        List<TrainingRecord> completedRecords = Arrays.asList(
                createTestRecord(1L, 2, 90, 120),
                createTestRecord(2L, 2, 85, 90));

        when(recordMapper.selectList(any())).thenReturn(completedRecords);

        // 执行测试
        Map<Integer, Boolean> result = trainingRecordService.batchCheckTrainingStatus(teacherIds, materialId);

        // 验证结果
        assertEquals(3, result.size());
        assertTrue(result.get(1));
        assertTrue(result.get(2));
        assertFalse(result.get(3));
        verify(recordMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("批量检查培训状态 - 参数无效")
    void batchCheckTrainingStatus_InvalidParams() {
        assertThrows(BusinessException.class, () -> trainingRecordService.batchCheckTrainingStatus(null, 1L));
        assertThrows(BusinessException.class,
                () -> trainingRecordService.batchCheckTrainingStatus(Collections.emptyList(), 1L));
        assertThrows(BusinessException.class,
                () -> trainingRecordService.batchCheckTrainingStatus(Arrays.asList(1, 2), null));
    }

    private TrainingRecord createTestRecord(Long recordId, Integer status, Integer score, Integer studyTime) {
        TrainingRecord record = new TrainingRecord();
        record.setRecordId(recordId);
        record.setStatus(status);
        record.setExamScore(score);
        record.setStudyTime(studyTime);
        return record;
    }
}