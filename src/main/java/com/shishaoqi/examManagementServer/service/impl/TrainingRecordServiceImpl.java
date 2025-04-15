package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
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
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord>
        implements TrainingRecordService {

    @Override
    public Page<TrainingRecord> search(String keyword, Page<TrainingRecord> page) {
        QueryWrapper<TrainingRecord> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like("title", keyword)
                    .or()
                    .like("description", keyword);
        }
        return baseMapper.selectPage(page, queryWrapper);
    }

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
        record.setProgress(0);
        record.setStatus(TrainingRecordStatus.NOT_STARTED);
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
        TrainingRecord record = getOne(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
                .orderByDesc(TrainingRecord::getCompleteTime)
                .last("LIMIT 1"));

        return record != null && record.getCompleteTime() != null;
    }

    @Override
    public boolean updateScore(Long recordId, Integer score, TrainingRecordStatus status) {
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
        if (record.getStatus() == TrainingRecordStatus.COMPLETED) {
            log.warn("培训已完成，不能修改成绩，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        if (score < 0 || score > 100) {
            log.warn("无效的分数：{}", score);
            throw new BusinessException(ErrorCode.INVALID_SCORE);
        }

        record.setProgress(score);
        record.setStatus(status);
        if (status == TrainingRecordStatus.COMPLETED) {
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
    public boolean updateProgress(Long recordId, Integer teacherId, Integer progress) {
        log.info("开始更新学习进度，记录ID：{}，教师ID：{}，进度：{}", recordId, teacherId, progress);

        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }

        if (!record.getTeacherId().equals(teacherId)) {
            log.warn("无权更新此培训记录，记录ID：{}，教师ID：{}", recordId, teacherId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (progress < 0 || progress > 100) {
            log.warn("无效的进度值：{}", progress);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "进度值必须在0-100之间");
        }

        record.setProgress(progress);
        record.setLastAccess(LocalDateTime.now());

        // 更新状态
        if (record.getStatus() == TrainingRecordStatus.NOT_STARTED && progress > 0) {
            record.setStatus(TrainingRecordStatus.IN_PROGRESS);
        } else if (progress == 100) {
            record.setStatus(TrainingRecordStatus.COMPLETED);
            record.setCompleteTime(LocalDateTime.now());
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

        boolean isValid = record.getProgress() >= material.getDuration();
        log.info("验证学习时长结果：{}，记录ID：{}，实际学习进度：{}，要求时长：{}",
                isValid ? "通过" : "不通过", recordId, record.getProgress(), material.getDuration());

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

        // 获取总培训时长
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId);
        List<TrainingRecord> records = list(wrapper);
        int totalProgress = records.stream()
                .mapToInt(TrainingRecord::getProgress)
                .sum();
        statistics.put("totalProgress", totalProgress);

        // 获取已完成培训数量
        long completedCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.COMPLETED)
                .count();
        statistics.put("completedCount", completedCount);

        return statistics;
    }

    @Override
    public List<TrainingRecord> getUnfinishedTrainings(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .ne(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
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
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
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
        LambdaQueryWrapper<TrainingRecord> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(TrainingRecord::getTeacherId, teacherId);
        long total = count(totalWrapper);

        if (total == 0) {
            return 0.0;
        }

        LambdaQueryWrapper<TrainingRecord> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(TrainingRecord::getTeacherId, teacherId);
        completedWrapper.eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED);
        long completed = count(completedWrapper);

        return (completed * 100.0) / total;
    }

    @Override
    public double getAverageScore(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
                .isNotNull(TrainingRecord::getProgress);

        List<TrainingRecord> records = list(wrapper);
        if (records.isEmpty()) {
            return 0.0;
        }

        return records.stream()
                .mapToInt(TrainingRecord::getProgress)
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
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
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
                        .eq(TrainingMaterial::getIsRequired, true)
                        .eq(TrainingMaterial::getStatus, "PUBLISHED"));

        // 检查每个必修培训的完成情况
        List<Map<String, Object>> details = new ArrayList<>();
        int completedCount = 0;

        for (TrainingMaterial material : requiredMaterials) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("materialId", material.getMaterialId());
            detail.put("title", material.getTitle());

            // 获取最新的培训记录
            TrainingRecord record = getOne(new LambdaQueryWrapper<TrainingRecord>()
                    .eq(TrainingRecord::getTeacherId, teacherId)
                    .eq(TrainingRecord::getMaterialId, material.getMaterialId())
                    .orderByDesc(TrainingRecord::getCompleteTime)
                    .last("LIMIT 1"));

            boolean completed = record != null &&
                    record.getStatus() == TrainingRecordStatus.COMPLETED &&
                    !isTrainingExpired(teacherId, material.getMaterialId());

            detail.put("completed", completed);
            detail.put("expired", record != null && isTrainingExpired(teacherId, material.getMaterialId()));
            details.add(detail);

            if (completed) {
                completedCount++;
            }
        }

        int totalRequired = requiredMaterials.size();
        double completionRate = totalRequired > 0 ? (double) completedCount / totalRequired : 0.0;

        status.put("details", details);
        status.put("totalRequired", totalRequired);
        status.put("completedCount", completedCount);
        status.put("completionRate", completionRate);

        return status;
    }

    @Override
    public Map<String, Object> getTrainingCertificate(Integer teacherId, Long materialId) {
        Map<String, Object> certificate = new HashMap<>();

        // 获取最新的完成记录
        TrainingRecord record = getOne(new LambdaQueryWrapper<TrainingRecord>()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
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
        certificate.put("score", record.getProgress());
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
                        .ne(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)) > 0;

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
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED));

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

        if (record.getStatus() == TrainingRecordStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.TRAINING_ALREADY_COMPLETED);
        }

        record.setProgress(0);
        record.setStatus(TrainingRecordStatus.NOT_STARTED);
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
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
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

    @Override
    public List<TrainingRecord> getTeacherTrainings(Integer teacherId, TrainingRecordStatus status) {
        log.info("获取教师{}的培训记录，状态：{}", teacherId, status);

        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId);
        if (status != null) {
            wrapper.eq(TrainingRecord::getStatus, status);
        }

        // 获取培训记录列表
        List<TrainingRecord> records = baseMapper.selectList(wrapper);

        // 使用关联查询获取培训材料信息
        for (TrainingRecord record : records) {
            TrainingMaterial material = trainingMaterialService.getById(record.getMaterialId());
            if (material == null) {
                log.warn("培训材料不存在，材料ID：{}", record.getMaterialId());
            }
        }

        return records;
    }

    @Override
    @Transactional
    public void startTraining(Long recordId, Integer teacherId) {
        log.info("开始培训，记录ID：{}，教师ID：{}", recordId, teacherId);

        TrainingRecord record = getById(recordId);
        if (record == null) {
            log.warn("培训记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.TRAINING_NOT_FOUND);
        }

        if (!record.getTeacherId().equals(teacherId)) {
            log.warn("无权开始此培训，记录ID：{}，教师ID：{}", recordId, teacherId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (record.getStatus() != TrainingRecordStatus.NOT_STARTED) {
            log.warn("培训已经开始或完成，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        record.setStatus(TrainingRecordStatus.IN_PROGRESS);
        record.setStartTime(LocalDateTime.now());
        record.setLastAccess(LocalDateTime.now());

        updateById(record);
        log.info("成功开始培训，记录ID：{}", recordId);
    }

    @Override
    public TrainingRecord getTrainingRecord(Long recordId, Integer teacherId) {
        return lambdaQuery()
                .eq(TrainingRecord::getRecordId, recordId)
                .eq(TrainingRecord::getTeacherId, teacherId)
                .one();
    }

    @Override
    public Map<String, Object> getTrainingStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 获取时间范围内的所有培训记录
        List<TrainingRecord> records = lambdaQuery()
                .ge(TrainingRecord::getStartTime, startDate)
                .le(TrainingRecord::getCompleteTime, endDate)
                .list();

        // 统计各种状态的培训数量
        long activeCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.IN_PROGRESS)
                .count();
        long completedCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.COMPLETED)
                .count();
        long totalCount = records.size();

        // 计算完成率
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;

        // 统计培训中的教师数量
        long inTrainingCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.IN_PROGRESS)
                .map(TrainingRecord::getTeacherId)
                .distinct()
                .count();

        stats.put("activeCount", activeCount);
        stats.put("completedCount", completedCount);
        stats.put("totalCount", totalCount);
        stats.put("completionRate", completionRate);
        stats.put("inTrainingCount", inTrainingCount);

        return stats;
    }

    @Override
    public Map<String, Object> getTeacherTrainingStatistics(Integer teacherId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取教师的所有培训记录
        List<TrainingRecord> records = lambdaQuery()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .list();

        // 统计各种状态的培训数量
        long pendingCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.NOT_STARTED)
                .count();
        long completedCount = records.stream()
                .filter(r -> r.getStatus() == TrainingRecordStatus.COMPLETED)
                .count();
        long totalCount = records.size();

        // 计算完成率
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;

        stats.put("pendingCount", pendingCount);
        stats.put("completedCount", completedCount);
        stats.put("totalCount", totalCount);
        stats.put("completionRate", completionRate);

        return stats;
    }

    @Override
    public List<TrainingRecord> getTeacherTrainingRecords(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return lambdaQuery()
                .eq(TrainingRecord::getTeacherId, teacherId)
                .orderByDesc(TrainingRecord::getStartTime)
                .list();
    }

    @Override
    public Page<TrainingRecord> getPage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "页码和每页大小必须大于0");
        }

        Page<TrainingRecord> pageParam = new Page<>(page, size);
        Page<TrainingRecord> result = lambdaQuery()
                .orderByDesc(TrainingRecord::getStartTime)
                .page(pageParam);

        log.info("分页查询结果 - 当前页: {}, 每页大小: {}, 总记录数: {}, 总页数: {}",
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages());

        return result;
    }

    @Override
    public Map<String, Object> getTrainingMaterialTags(Long materialId) {
        Map<String, Object> tagInfo = new HashMap<>();

        TrainingMaterial material = trainingMaterialService.getById(materialId);
        if (material == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 获取材料的标签
        String[] tags = material.getTags() != null ? material.getTags().split(",") : new String[0];

        // 获取使用该标签的培训记录数量
        Map<String, Long> tagUsage = new HashMap<>();
        for (String tag : tags) {
            long count = lambdaQuery()
                    .eq(TrainingRecord::getMaterialId, materialId)
                    .count();
            tagUsage.put(tag.trim(), count);
        }

        // 获取相关的培训材料
        List<Long> relatedMaterialIds = trainingMaterialService.list(new LambdaQueryWrapper<TrainingMaterial>()
                .like(TrainingMaterial::getTags, String.join(",", tags))
                .ne(TrainingMaterial::getMaterialId, materialId)
                .last("LIMIT 5"))
                .stream()
                .map(TrainingMaterial::getMaterialId)
                .collect(Collectors.toList());

        tagInfo.put("tags", tags);
        tagInfo.put("tagUsage", tagUsage);
        tagInfo.put("relatedMaterials", relatedMaterialIds);

        return tagInfo;
    }

    @Override
    public Map<String, Object> previewTrainingMaterial(Long materialId) {
        Map<String, Object> preview = new HashMap<>();

        TrainingMaterial material = trainingMaterialService.getById(materialId);
        if (material == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 获取材料基本信息
        preview.put("title", material.getTitle());
        preview.put("content", material.getContent());
        preview.put("duration", material.getDuration());
        preview.put("tags", material.getTags());

        // 获取学习统计信息
        long totalLearners = lambdaQuery()
                .eq(TrainingRecord::getMaterialId, materialId)
                .groupBy(TrainingRecord::getTeacherId)
                .count();

        long completedLearners = lambdaQuery()
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
                .groupBy(TrainingRecord::getTeacherId)
                .count();

        // 计算平均完成时间
        List<TrainingRecord> completedRecords = lambdaQuery()
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED)
                .list();

        double avgCompletionTime = completedRecords.stream()
                .mapToLong(r -> {
                    if (r.getStartTime() != null && r.getCompleteTime() != null) {
                        return java.time.Duration.between(r.getStartTime(), r.getCompleteTime()).toMinutes();
                    }
                    return 0L;
                })
                .average()
                .orElse(0.0);

        preview.put("totalLearners", totalLearners);
        preview.put("completedLearners", completedLearners);
        preview.put("completionRate", totalLearners > 0 ? (double) completedLearners / totalLearners : 0);
        preview.put("averageCompletionTime", avgCompletionTime);

        return preview;
    }
}