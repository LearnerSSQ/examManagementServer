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

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    public TrainingRecordServiceImpl(TrainingMaterialMapper materialMapper) {
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

        if (recordId == null || score == null || status == null) {
            log.error("参数不能为空：recordId={}, score={}, status={}", recordId, score, status);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }

        // 验证状态转换的合法性
        if (record.getStatus() == 2) {
            log.warn("培训已完成，不能修改成绩，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        if (score < 0 || score > 100) {
            log.warn("无效的分数：{}", score);
            throw new BusinessException(ErrorCode.INVALID_SCORE);
        }

        if (status != 1 && status != 2) {
            log.warn("无效的状态值：{}", status);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        record.setExamScore(score);
        record.setStatus(status);
        if (status == 2) { // 如果状态为完成，更新完成时间
            record.setCompleteTime(LocalDateTime.now());
        }

        boolean success = updateById(record);
        if (success) {
            log.info("成功更新培训成绩，记录ID：{}，分数：{}，状态：{}", recordId, score, status);
        } else {
            log.error("更新培训成绩失败，记录ID：{}，分数：{}，状态：{}", recordId, score, status);
        }
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

        boolean isValid = record.getStudyTime() >= material.getRequiredMinutes();
        log.info("验证学习时长结果：{}，记录ID：{}，实际学习时长：{}，要求时长：{}",
                isValid ? "通过" : "不通过", recordId, record.getStudyTime(), material.getRequiredMinutes());

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
            // 设置固定的培训有效期为6个月
            LocalDateTime expiryDate = record.getCompleteTime().plusMonths(6);
            return LocalDateTime.now().isAfter(expiryDate);
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
        if (teacherIds == null || teacherIds.isEmpty() || materialId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<Integer, Boolean> result = new HashMap<>();

        // 批量查询所有教师的培训记录
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getMaterialId, materialId)
                .in(TrainingRecord::getTeacherId, teacherIds)
                .eq(TrainingRecord::getStatus, 2) // 只查询已完成的记录
                .orderByDesc(TrainingRecord::getCompleteTime);

        List<TrainingRecord> records = list(wrapper);

        // 将查询结果转换为Map，每个教师只取最新的一条记录
        Map<Integer, TrainingRecord> latestRecords = records.stream()
                .collect(Collectors.groupingBy(
                        TrainingRecord::getTeacherId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.isEmpty() ? null : list.get(0))));

        // 检查每个教师的培训状态
        for (Integer teacherId : teacherIds) {
            TrainingRecord record = latestRecords.get(teacherId);
            if (record == null) {
                // 没有培训记录
                result.put(teacherId, false);
                continue;
            }

            // 检查培训是否过期
            boolean isExpired = isTrainingExpired(teacherId, materialId);
            result.put(teacherId, !isExpired);
        }

        return result;
    }

    @Override
    public Map<String, Object> getRequiredTrainingStatus(Integer teacherId) {
        Map<String, Object> status = new HashMap<>();

        // 获取所有必修培训材料
        List<TrainingMaterial> requiredMaterials = trainingMaterialService
                .list(new LambdaQueryWrapper<TrainingMaterial>()
                        .eq(TrainingMaterial::getType, 1)); // 假设type=1为必修培训

        // 检查每个必修培训的完成情况
        List<Map<String, Object>> details = new ArrayList<>();
        for (TrainingMaterial material : requiredMaterials) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("materialId", material.getMaterialId());
            detail.put("title", material.getTitle());
            detail.put("completed", hasCompletedTraining(teacherId, material.getMaterialId()));
            detail.put("expired", isTrainingExpired(teacherId, material.getMaterialId()));
            details.add(detail);
        }

        status.put("details", details);
        status.put("totalRequired", requiredMaterials.size());
        status.put("completedCount", details.stream()
                .filter(d -> (boolean) d.get("completed") && !(boolean) d.get("expired"))
                .count());

        return status;
    }

    @Override
    public Map<String, Object> getTrainingCertificate(Integer teacherId, Long materialId) {
        Map<String, Object> certificate = new HashMap<>();

        // 获取最新的完成记录
        TrainingRecord record = getOne(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, 2)
                .orderByDesc(TrainingRecord::getCompleteTime)
                .last("LIMIT 1"));

        if (record == null || isTrainingExpired(teacherId, materialId)) {
            throw new BusinessException(ErrorCode.TRAINING_EXPIRED);
        }

        TrainingMaterial material = trainingMaterialService.getById(materialId);
        if (material == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        certificate.put("certificateId", UUID.randomUUID().toString());
        certificate.put("teacherId", teacherId);
        certificate.put("materialTitle", material.getTitle());
        certificate.put("completionDate", record.getCompleteTime());
        certificate.put("score", record.getExamScore());
        certificate.put("expiryDate", record.getCompleteTime().plusMonths(6));

        return certificate;
    }

    @Override
    public boolean assignTrainingBatch(List<Integer> teacherIds, Long materialId, LocalDateTime deadline) {
        if (teacherIds == null || teacherIds.isEmpty() || materialId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        TrainingMaterial material = trainingMaterialService.getById(materialId);
        if (material == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        try {
            for (Integer teacherId : teacherIds) {
                // 检查是否已有未完成的记录
                boolean hasUnfinished = count(new LambdaQueryWrapper<TrainingRecord>()
                        .eq(TrainingRecord::getTeacherId, teacherId)
                        .eq(TrainingRecord::getMaterialId, materialId)
                        .ne(TrainingRecord::getStatus, 2)) > 0;

                if (!hasUnfinished) {
                    createRecord(teacherId, materialId);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("批量分配培训任务失败", e);
            return false;
        }
    }

    @Override
    public List<TrainingRecord> getExpiringTrainings(Integer teacherId, int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);

        // 获取所有已完成的培训记录
        List<TrainingRecord> completedRecords = list(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getStatus, 2));

        // 筛选即将过期的记录
        return completedRecords.stream()
                .filter(record -> {
                    LocalDateTime expiryDate = record.getCompleteTime().plusMonths(6);
                    return LocalDateTime.now().isBefore(expiryDate) &&
                            expiryDate.isBefore(thresholdDate);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean resetTrainingProgress(Long recordId) {
        TrainingRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }

        if (record.getStatus() == 2) {
            throw new BusinessException(ErrorCode.TRAINING_ALREADY_COMPLETED);
        }

        record.setStudyTime(0);
        record.setStatus(0);
        record.setExamScore(null);
        record.setStartTime(LocalDateTime.now());
        record.setCompleteTime(null);

        return updateById(record);
    }

    @Override
    public List<TrainingRecord> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return lambdaQuery()
                .ge(TrainingRecord::getStartTime, startTime)
                .le(TrainingRecord::getStartTime, endTime)
                .orderByDesc(TrainingRecord::getStartTime)
                .list();
    }

    @Override
    public List<TrainingRecord> getTeacherRecordsByTimeRange(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime) {
        if (teacherId == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return lambdaQuery()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .ge(TrainingRecord::getStartTime, startTime)
                .le(TrainingRecord::getStartTime, endTime)
                .orderByDesc(TrainingRecord::getStartTime)
                .list();
    }

    @Override
    public Map<String, Object> getTrainingStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> statistics = new HashMap<>();

        // 获取总记录数
        long totalRecords = lambdaQuery()
                .ge(TrainingRecord::getStartTime, startTime)
                .le(TrainingRecord::getStartTime, endTime)
                .count();

        // 获取已完成培训数量
        long completedTrainings = lambdaQuery()
                .ge(TrainingRecord::getStartTime, startTime)
                .le(TrainingRecord::getStartTime, endTime)
                .eq(TrainingRecord::getStatus, 2) // 假设2表示已完成
                .count();

        // 获取参与培训的教师数量
        long teacherCount = lambdaQuery()
                .ge(TrainingRecord::getStartTime, startTime)
                .le(TrainingRecord::getStartTime, endTime)
                .groupBy(TrainingRecord::getTeacherId)
                .count();

        statistics.put("totalRecords", totalRecords);
        statistics.put("completedTrainings", completedTrainings);
        statistics.put("teacherCount", teacherCount);
        statistics.put("completionRate", totalRecords > 0 ? (double) completedTrainings / totalRecords : 0);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);

        return statistics;
    }
}