package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TrainingRecordMapper;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord>
        implements TrainingRecordService {

    private static final Logger log = LoggerFactory.getLogger(TrainingRecordServiceImpl.class);

    private final TrainingMaterialMapper materialMapper;

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    public TrainingRecordServiceImpl(TrainingMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public TrainingRecord createRecord(Integer teacherId, Long materialId) {
        TrainingRecord record = new TrainingRecord();
        record.setTeacherId(teacherId);
        record.setMaterialId(materialId);
        record.setStudyTime(0);
        record.setStatus(0); // 未开始
        record.setStartTime(LocalDateTime.now());
        save(record);
        return record;
    }

    @Override
    public List<TrainingRecord> getTeacherRecords(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public List<TrainingRecord> getMaterialRecords(Long materialId) {
        if (materialId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getMaterialId, materialId)
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public boolean hasCompletedTraining(Integer teacherId, Long materialId) {
        if (teacherId == null || materialId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, 2); // 2 表示已通过
        return count(wrapper) > 0;
    }

    @Override
    public boolean updateScore(Long recordId, Integer score, Integer status) {
        log.info("开始更新培训成绩，记录ID：{}，分数：{}，状态：{}", recordId, score, status);
        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }
        if (score < 0 || score > 100) {
            log.warn("无效的分数：{}", score);
            throw new BusinessException(ErrorCode.INVALID_SCORE);
        }
        record.setExamScore(score);
        record.setStatus(status);
        record.setCompleteTime(LocalDateTime.now());
        boolean success = updateById(record);
        log.info("更新培训成绩结果：{}，记录ID：{}", success ? "成功" : "失败", recordId);
        return success;
    }

    @Override
    public boolean updateStudyProgress(Long recordId, Integer studyTime) {
        log.info("开始更新学习进度，记录ID：{}，学习时长：{}", recordId, studyTime);
        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }
        if (studyTime < 0) {
            log.warn("无效的学习时长：{}", studyTime);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        record.setStudyTime(studyTime);
        record.setCompleteTime(LocalDateTime.now());
        if (record.getStatus() == 0) {
            record.setStatus(1); // 更新为进行中
        }
        boolean success = updateById(record);
        log.info("更新学习进度结果：{}，记录ID：{}", success ? "成功" : "失败", recordId);
        return success;
    }

    @Override
    public boolean validateStudyTime(Long recordId) {
        log.info("开始验证学习时长，记录ID：{}", recordId);
        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }

        TrainingMaterial material = trainingMaterialService.getById(record.getMaterialId());
        if (material == null) {
            log.warn("培训材料不存在，材料ID：{}", record.getMaterialId());
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        boolean isValid = record.getStudyTime() >= material.getRequiredTime();
        log.info("验证学习时长结果：{}，记录ID：{}，实际学习时长：{}，要求时长：{}",
                isValid ? "通过" : "不通过", recordId, record.getStudyTime(), material.getRequiredTime());

        if (!isValid) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STUDY_TIME);
        }
        return true;
    }

    @Override
    public Map<String, Object> getTrainingStatistics(Integer teacherId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取完成率
        statistics.put("completionRate", getCompletionRate(teacherId));

        // 获取平均分
        statistics.put("averageScore", getAverageScore(teacherId));

        // 获取总培训时长
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId);
        List<TrainingRecord> records = list(wrapper);
        int totalStudyTime = records.stream()
                .mapToInt(TrainingRecord::getStudyTime)
                .sum();
        statistics.put("totalStudyTime", totalStudyTime);

        // 获取已完成培训数量
        long completedCount = records.stream()
                .filter(r -> r.getStatus() == 2)
                .count();
        statistics.put("completedCount", completedCount);

        return statistics;
    }

    @Override
    public List<TrainingRecord> getUnfinishedTrainings(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .ne(TrainingRecord::getStatus, 2) // 未完成的培训
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public List<TrainingRecord> getRecentTrainings(Integer teacherId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .ge(TrainingRecord::getStartTime, startDate)
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public boolean isTrainingExpired(Integer teacherId, Long materialId) {
        TrainingRecord record = getOne(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, 2)
                .orderByDesc(TrainingRecord::getCompleteTime)
                .last("LIMIT 1"));

        if (record != null && record.getCompleteTime() != null) {
            TrainingMaterial material = materialMapper.selectById(materialId);
            if (material != null && material.getValidityPeriod() != null) {
                LocalDateTime expiryDate = record.getCompleteTime().plusMonths(material.getValidityPeriod());
                return LocalDateTime.now().isAfter(expiryDate);
            }
        }
        return true; // 如果没有完成记录，视为已过期
    }

    @Override
    public double getCompletionRate(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId);
        long total = count(wrapper);

        if (total == 0) {
            return 0.0;
        }

        wrapper.eq(TrainingRecord::getStatus, 2); // 已完成
        long completed = count(wrapper);

        return (completed * 100.0) / total;
    }

    @Override
    public double getAverageScore(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getStatus, 2) // 只计算已完成的
                .isNotNull(TrainingRecord::getExamScore);

        List<TrainingRecord> records = list(wrapper);
        if (records.isEmpty()) {
            return 0.0;
        }

        return records.stream()
                .mapToInt(TrainingRecord::getExamScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public Map<Integer, Boolean> batchCheckTrainingStatus(List<Integer> teacherIds, Long materialId) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TrainingRecord::getTeacherId, teacherIds)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, 2);

        List<TrainingRecord> completedRecords = list(wrapper);
        Map<Integer, Boolean> result = teacherIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> false));

        completedRecords.forEach(record -> result.put(record.getTeacherId(), true));

        return result;
    }
}