package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.InvigilationRecordMapper;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@CacheConfig(cacheNames = "invigilation_record")
public class InvigilationRecordServiceImpl extends ServiceImpl<InvigilationRecordMapper, InvigilationRecord>
        implements InvigilationRecordService {

    private static final Logger log = LoggerFactory.getLogger(InvigilationRecordServiceImpl.class);

    // 记录类型常量
    public static final int RECORD_TYPE_SIGN_IN = 1;
    public static final int RECORD_TYPE_EXCEPTION = 2;
    public static final int RECORD_TYPE_NOTE = 3;

    @Autowired
    private InvigilatorAssignmentMapper assignmentMapper;

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Override
    @Cacheable(key = "'assignment_' + #assignmentId")
    public List<InvigilationRecord> getRecordsByAssignment(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取监考记录失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "获取监考记录");

        log.info("获取监考记录列表，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条监考记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    @Override
    @Cacheable(key = "'sign_in_' + #assignmentId")
    public InvigilationRecord getSignInRecord(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取签到记录失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "获取签到记录");

        log.info("获取签到记录，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, RECORD_TYPE_SIGN_IN)
                .orderByDesc(InvigilationRecord::getCreateTime)
                .last("LIMIT 1");

        InvigilationRecord record = getOne(wrapper);
        log.info("获取签到记录{}，监考安排ID：{}", record != null ? "成功" : "失败", assignmentId);
        return record;
    }

    @Override
    @Cacheable(key = "'exceptions_' + #assignmentId")
    public List<InvigilationRecord> getExceptionRecords(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取异常记录失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "获取异常记录");

        log.info("获取异常记录列表，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, RECORD_TYPE_EXCEPTION)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条异常记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    @Override
    @Cacheable(key = "'has_signed_in_' + #assignmentId")
    public boolean hasSignedIn(Long assignmentId) {
        if (assignmentId == null) {
            log.error("检查签到状态失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "检查签到状态");

        log.info("检查签到状态，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, RECORD_TYPE_SIGN_IN);

        long count = count(wrapper);
        log.info("签到状态检查完成，监考安排ID：{}，是否已签到：{}", assignmentId, count > 0);
        return count > 0;
    }

    @Override
    @Cacheable(key = "'exception_count_' + #assignmentId")
    public int countExceptionRecords(Long assignmentId) {
        if (assignmentId == null) {
            log.error("统计异常记录失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "统计异常记录");

        log.info("统计异常记录数量，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, RECORD_TYPE_EXCEPTION);

        long count = count(wrapper);
        log.info("异常记录统计完成，监考安排ID：{}，异常记录数量：{}", assignmentId, count);
        return (int) count;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean save(InvigilationRecord record) {
        validateRecord(record);
        validateAssignment(record.getAssignmentId(), "创建监考记录");

        // 检查签到记录的唯一性
        if (record.getType() == RECORD_TYPE_SIGN_IN && hasSignedIn(record.getAssignmentId())) {
            log.error("创建监考记录失败：已存在签到记录，安排ID：{}", record.getAssignmentId());
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "已存在签到记录");
        }

        boolean success = super.save(record);
        if (success) {
            log.info("成功创建监考记录，类型：{}，监考安排ID：{}", record.getType(), record.getAssignmentId());
        } else {
            log.error("创建监考记录失败，类型：{}，监考安排ID：{}", record.getType(), record.getAssignmentId());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    private void validateRecord(InvigilationRecord record) {
        if (record == null) {
            log.error("监考记录验证失败：记录对象为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (record.getAssignmentId() == null) {
            log.error("监考记录验证失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (record.getType() == null) {
            log.error("监考记录验证失败：记录类型为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (record.getType() < RECORD_TYPE_SIGN_IN || record.getType() > RECORD_TYPE_NOTE) {
            log.error("监考记录验证失败：无效的记录类型：{}", record.getType());
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的记录类型");
        }
        if (record.getDescription() == null || record.getDescription().trim().isEmpty()) {
            log.error("监考记录验证失败：记录描述为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "记录描述不能为空");
        }
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean updateById(InvigilationRecord record) {
        if (record == null || record.getRecordId() == null) {
            log.error("更新监考记录失败：记录ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证记录是否存在
        InvigilationRecord existingRecord = getById(record.getRecordId());
        if (existingRecord == null) {
            log.error("更新监考记录失败：记录不存在，记录ID：{}", record.getRecordId());
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 验证必要字段
        validateRecord(record);

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(record.getAssignmentId());
        if (assignment == null) {
            log.error("更新监考记录失败：监考安排不存在，安排ID：{}", record.getAssignmentId());
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 如果是签到记录，检查唯一性（排除自身）
        if (record.getType() == 1) {
            LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InvigilationRecord::getAssignmentId, record.getAssignmentId())
                    .eq(InvigilationRecord::getType, 1)
                    .ne(InvigilationRecord::getRecordId, record.getRecordId());
            if (count(wrapper) > 0) {
                log.error("更新监考记录失败：已存在其他签到记录，安排ID：{}", record.getAssignmentId());
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "已存在其他签到记录");
            }
        }

        boolean success = super.updateById(record);
        log.info("更新监考记录{}，ID：{}，类型：{}，监考安排ID：{}",
                success ? "成功" : "失败", record.getRecordId(), record.getType(), record.getAssignmentId());
        return success;
    }

    /**
     * 批量删除监考记录
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean removeByIds(Collection<?> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            log.error("批量删除监考记录失败：记录ID列表为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        boolean success = super.removeByIds(recordIds);
        log.info("批量删除监考记录{}，记录数量：{}", success ? "成功" : "失败", recordIds.size());
        return success;
    }

    /**
     * 获取指定时间范围内的异常记录
     */
    @Override
    @Cacheable(key = "'time_range_' + #assignmentId + '_' + #startTime + '_' + #endTime")
    public List<InvigilationRecord> getExceptionRecordsByTimeRange(Long assignmentId, LocalDateTime startTime,
            LocalDateTime endTime) {
        if (assignmentId == null || startTime == null || endTime == null) {
            log.error("获取时间范围内的异常记录失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        if (startTime.isAfter(endTime)) {
            log.error("获取时间范围内的异常记录失败：开始时间晚于结束时间");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能晚于结束时间");
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取时间范围内的异常记录失败：监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        log.info("获取时间范围内的异常记录，监考安排ID：{}，时间范围：{} 至 {}", assignmentId, startTime, endTime);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, 2)
                .between(InvigilationRecord::getCreateTime, startTime, endTime)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条时间范围内的异常记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    /**
     * 获取最近的异常记录
     */
    @Override
    @Cacheable(key = "'recent_exceptions_' + #assignmentId + '_' + #limit")
    public List<InvigilationRecord> getRecentExceptionRecords(Long assignmentId, int limit) {
        if (assignmentId == null || limit <= 0) {
            log.error("获取最近异常记录失败：参数错误");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取最近异常记录失败：监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        log.info("获取最近的异常记录，监考安排ID：{}，限制数量：{}", assignmentId, limit);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, 2)
                .orderByDesc(InvigilationRecord::getCreateTime)
                .last("LIMIT " + limit);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条最近的异常记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    @Override
    @Cacheable(key = "'statistics_' + #assignmentId")
    public Map<String, Object> getRecordStatistics(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取监考记录统计信息失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取监考记录统计信息失败：监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        log.info("获取监考记录统计信息，监考安排ID：{}", assignmentId);
        Map<String, Object> statistics = new HashMap<>();

        // 获取总记录数
        LambdaQueryWrapper<InvigilationRecord> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(InvigilationRecord::getAssignmentId, assignmentId);
        long totalCount = count(totalWrapper);
        statistics.put("totalCount", totalCount);

        // 获取异常记录数
        statistics.put("exceptionCount", countExceptionRecords(assignmentId));

        // 检查是否签到
        statistics.put("hasSignedIn", hasSignedIn(assignmentId));

        // 获取最后一条记录时间
        if (totalCount > 0) {
            LambdaQueryWrapper<InvigilationRecord> lastWrapper = new LambdaQueryWrapper<>();
            lastWrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                    .orderByDesc(InvigilationRecord::getCreateTime)
                    .last("LIMIT 1");
            InvigilationRecord lastRecord = getOne(lastWrapper);
            statistics.put("lastRecordTime", lastRecord.getCreateTime());
        }

        log.info("监考记录统计信息获取完成，监考安排ID：{}", assignmentId);
        return statistics;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean saveBatch(List<InvigilationRecord> records) {
        if (records == null || records.isEmpty()) {
            log.error("批量创建监考记录失败：记录列表为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证所有记录
        for (InvigilationRecord record : records) {
            validateRecord(record);
        }

        // 获取所有监考安排ID
        Set<Long> assignmentIds = records.stream()
                .map(InvigilationRecord::getAssignmentId)
                .collect(Collectors.toSet());

        // 验证所有监考安排是否存在
        List<InvigilatorAssignment> assignments = assignmentMapper.selectBatchIds(assignmentIds);
        if (assignments.size() != assignmentIds.size()) {
            log.error("批量创建监考记录失败：部分监考安排不存在");
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 检查签到记录的唯一性
        for (InvigilationRecord record : records) {
            if (record.getType() == 1 && hasSignedIn(record.getAssignmentId())) {
                log.error("批量创建监考记录失败：监考安排已存在签到记录，安排ID：{}", record.getAssignmentId());
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "监考安排已存在签到记录");
            }
        }

        boolean success = super.saveBatch(records);
        log.info("批量创建监考记录{}，记录数量：{}", success ? "成功" : "失败", records.size());
        return success;
    }

    @Override
    @Cacheable(key = "'type_' + #assignmentId + '_' + #type")
    public List<InvigilationRecord> getRecordsByType(Long assignmentId, Integer type) {
        if (assignmentId == null || type == null) {
            log.error("获取指定类型的监考记录失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        if (type < RECORD_TYPE_SIGN_IN || type > RECORD_TYPE_NOTE) {
            log.error("获取指定类型的监考记录失败：无效的记录类型：{}", type);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的记录类型");
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取指定类型的监考记录失败：监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        log.info("获取指定类型的监考记录，监考安排ID：{}，记录类型：{}", assignmentId, type);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, type)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条类型{}的监考记录，监考安排ID：{}", records.size(), type, assignmentId);
        return records;
    }

    @Override
    @Cacheable(key = "'recent_' + #assignmentId + '_' + #hours")
    public List<InvigilationRecord> getRecentRecords(Long assignmentId, int hours) {
        if (assignmentId == null || hours <= 0) {
            log.error("获取最近监考记录失败：参数无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "获取最近监考记录");

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        log.info("获取最近{}小时的监考记录，监考安排ID：{}", hours, assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .between(InvigilationRecord::getCreateTime, startTime, endTime)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条最近监考记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public InvigilationRecord recordSignIn(Long assignmentId, LocalDateTime signInTime) {
        if (assignmentId == null || signInTime == null) {
            log.error("记录签到失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "记录签到");

        // 检查是否已签到
        if (hasSignedIn(assignmentId)) {
            log.error("记录签到失败：已存在签到记录，监考安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "已存在签到记录");
        }

        InvigilationRecord record = new InvigilationRecord();
        record.setAssignmentId(assignmentId);
        record.setType(RECORD_TYPE_SIGN_IN);
        record.setDescription("签到时间：" + signInTime);
        record.setCreateTime(signInTime);

        save(record);
        log.info("成功记录签到，监考安排ID：{}", assignmentId);
        return record;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public InvigilationRecord recordExceptionEvent(Long assignmentId, Integer eventType, String description) {
        if (assignmentId == null || eventType == null || description == null) {
            log.error("记录异常事件失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "记录异常事件");

        InvigilationRecord record = new InvigilationRecord();
        record.setAssignmentId(assignmentId);
        record.setType(RECORD_TYPE_EXCEPTION);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());

        save(record);
        log.info("成功记录异常事件，监考安排ID：{}，事件类型：{}", assignmentId, eventType);
        return record;
    }

    @Override
    public Map<String, Object> getExceptionStatistics(Long examId) {
        if (examId == null) {
            log.error("获取异常事件统计失败：考试ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("获取异常事件统计，考试ID：{}", examId);
        Map<String, Object> statistics = new HashMap<>();

        // 获取异常记录总数
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getType, RECORD_TYPE_EXCEPTION);
        long totalExceptions = count(wrapper);
        statistics.put("totalExceptions", totalExceptions);

        return statistics;
    }

    @Override
    public List<InvigilationRecord> getAssignmentRecords(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取监考记录失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "获取监考记录");

        log.info("获取监考记录，监考安排ID：{}", assignmentId);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条监考记录，监考安排ID：{}", records.size(), assignmentId);
        return records;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean updateEventStatus(Long recordId, Integer status, String handlerComment) {
        if (recordId == null || status == null) {
            log.error("更新事件状态失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        InvigilationRecord record = getById(recordId);
        if (record == null) {
            log.error("更新事件状态失败：记录不存在，记录ID：{}", recordId);
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        record.setDescription(record.getDescription() + "\n处理意见：" + handlerComment);
        boolean success = updateById(record);
        log.info("更新事件状态{}，记录ID：{}", success ? "成功" : "失败", recordId);
        return success;
    }

    @Override
    public Map<String, Object> generateInvigilationReport(Long assignmentId) {
        if (assignmentId == null) {
            log.error("生成监考报告失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "生成监考报告");

        Map<String, Object> report = new HashMap<>();
        report.put("assignmentId", assignmentId);
        report.put("records", getAssignmentRecords(assignmentId));
        report.put("signInRecord", getSignInRecord(assignmentId));
        report.put("exceptionRecords", getExceptionRecords(assignmentId));
        report.put("statistics", getRecordStatistics(assignmentId));

        log.info("成功生成监考报告，监考安排ID：{}", assignmentId);
        return report;
    }

    @Override
    public List<Map<String, Object>> exportInvigilationRecords(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            log.error("导出监考记录失败：时间范围不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        if (startTime.isAfter(endTime)) {
            log.error("导出监考记录失败：开始时间晚于结束时间");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("导出监考记录，时间范围：{} 至 {}", startTime, endTime);
        List<Map<String, Object>> exportData = new ArrayList<>();

        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InvigilationRecord::getCreateTime, startTime, endTime)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        for (InvigilationRecord record : records) {
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", record.getRecordId());
            data.put("assignmentId", record.getAssignmentId());
            data.put("type", record.getType());
            data.put("description", record.getDescription());
            data.put("createTime", record.getCreateTime());
            exportData.add(data);
        }

        log.info("成功导出{}条监考记录", records.size());
        return exportData;
    }

    @Override
    public Map<String, Object> getTeacherInvigilationStats(Integer teacherId, int year) {
        if (teacherId == null || year < 2000) {
            log.error("获取教师监考统计失败：参数无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("获取教师监考统计，教师ID：{}，年份：{}", teacherId, year);
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59);

        // 获取监考记录总数
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InvigilationRecord::getCreateTime, startTime, endTime);
        long totalRecords = count(wrapper);
        stats.put("totalRecords", totalRecords);

        return stats;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public boolean recordInspectionResult(Long assignmentId, Map<String, Object> inspectionDetails) {
        if (assignmentId == null || inspectionDetails == null) {
            log.error("记录巡查结果失败：参数不完整");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        validateAssignment(assignmentId, "记录巡查结果");

        InvigilationRecord record = new InvigilationRecord();
        record.setAssignmentId(assignmentId);
        record.setType(RECORD_TYPE_NOTE);
        record.setDescription("巡查结果：" + inspectionDetails.toString());
        record.setCreateTime(LocalDateTime.now());

        boolean success = save(record);
        log.info("记录巡查结果{}，监考安排ID：{}", success ? "成功" : "失败", assignmentId);
        return success;
    }

    @Override
    public List<InvigilationRecord> getPendingExceptionEvents() {
        log.info("获取未处理的异常事件");
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getType, RECORD_TYPE_EXCEPTION)
                .orderByDesc(InvigilationRecord::getCreateTime);

        List<InvigilationRecord> records = list(wrapper);
        log.info("获取到{}条未处理的异常事件", records.size());
        return records;
    }

    private void validateAssignment(Long assignmentId, String operation) {
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("{}失败：监考安排不存在，安排ID：{}", operation, assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
    }

    @Override
    public List<InvigilationRecord> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .ge(InvigilationRecord::getCreateTime, startTime)
                .le(InvigilationRecord::getCreateTime, endTime)
                .orderByDesc(InvigilationRecord::getCreateTime)
                .list();
    }

    @Override
    public List<InvigilationRecord> getTeacherRecords(Integer teacherId) {
        // 先获取教师的所有监考安排ID
        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        // 根据监考安排ID查询相关记录
        return lambdaQuery()
                .in(InvigilationRecord::getAssignmentId, assignmentIds)
                .orderByDesc(InvigilationRecord::getCreateTime)
                .list();
    }

    @Override
    public List<InvigilationRecord> getTeacherRecordsByTimeRange(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime) {
        // 先获取教师在指定时间范围内的监考安排ID
        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        // 根据监考安排ID查询相关记录
        return lambdaQuery()
                .in(InvigilationRecord::getAssignmentId, assignmentIds)
                .ge(InvigilationRecord::getCreateTime, startTime)
                .le(InvigilationRecord::getCreateTime, endTime)
                .orderByDesc(InvigilationRecord::getCreateTime)
                .list();
    }

    @Override
    public Map<String, Object> getInvigilationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取总记录数
        long totalRecords = lambdaQuery()
                .ge(InvigilationRecord::getCreateTime, startTime)
                .le(InvigilationRecord::getCreateTime, endTime)
                .count();

        // 获取异常记录数
        long exceptionRecords = lambdaQuery()
                .ge(InvigilationRecord::getCreateTime, startTime)
                .le(InvigilationRecord::getCreateTime, endTime)
                .eq(InvigilationRecord::getType, 2) // 2表示异常事件
                .count();

        // 获取监考教师数量
        long teacherCount = assignmentService.lambdaQuery()
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .groupBy(InvigilatorAssignment::getTeacherId)
                .count();

        statistics.put("totalRecords", totalRecords);
        statistics.put("exceptionRecords", exceptionRecords);
        statistics.put("teacherCount", teacherCount);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);

        return statistics;
    }
}