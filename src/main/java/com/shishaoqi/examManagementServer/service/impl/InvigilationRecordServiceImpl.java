package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecord;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecordType;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.InvigilationRecordMapper;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import com.shishaoqi.examManagementServer.service.TeacherService;
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

    @Autowired
    private InvigilatorAssignmentMapper assignmentMapper;

    @Autowired
    private InvigilatorAssignmentService assignmentService;
    
    @Autowired
    private TeacherService teacherService;

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
                .eq(InvigilationRecord::getType, InvigilationRecordType.SIGN_IN)
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
                .eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT)
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
                .eq(InvigilationRecord::getType, InvigilationRecordType.SIGN_IN);

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
                .eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT);

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
        if (record.getType() == InvigilationRecordType.SIGN_IN && hasSignedIn(record.getAssignmentId())) {
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
        if (record.getContent() == null || record.getContent().trim().isEmpty()) {
            log.error("监考记录验证失败：记录内容为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "记录内容不能为空");
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
        if (record.getType() == InvigilationRecordType.SIGN_IN) {
            LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InvigilationRecord::getAssignmentId, record.getAssignmentId())
                    .eq(InvigilationRecord::getType, InvigilationRecordType.SIGN_IN)
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
                .eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT)
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
                .eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT)
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
            if (record.getType() == InvigilationRecordType.SIGN_IN && hasSignedIn(record.getAssignmentId())) {
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

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取指定类型的监考记录失败：监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        log.info("获取指定类型的监考记录，监考安排ID：{}，记录类型：{}", assignmentId, type);
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, InvigilationRecordType.values()[type])
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
        record.setType(InvigilationRecordType.SIGN_IN);
        record.setContent("签到时间：" + signInTime);
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
        record.setType(InvigilationRecordType.INCIDENT);
        record.setContent(description);
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
        wrapper.eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT);
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

        record.setContent(record.getContent() + "\n处理意见：" + handlerComment);
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
            data.put("content", record.getContent());
            data.put("createTime", record.getCreateTime());
            exportData.add(data);
        }

        log.info("成功导出{}条监考记录", records.size());
        return exportData;
    }

    @Override
    public Map<String, Object> getTeacherInvigilationStats(Integer teacherId, int year) {
        Map<String, Object> stats = new HashMap<>();

        // 获取指定年份的监考任务
        LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59);

        // 获取已完成的监考任务
        List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignmentsByTimeRange(
                teacherId, startTime, endTime).stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED)
                .collect(Collectors.toList());

        // 统计监考次数
        int totalRecords = assignments.size();
        stats.put("totalRecords", totalRecords);

        // 如果有监考任务，计算平均评分
        if (!assignments.isEmpty()) {
            // 获取所有已完成任务的ID
            List<Long> assignmentIds = assignments.stream()
                    .map(InvigilatorAssignment::getAssignmentId)
                    .collect(Collectors.toList());

            // 从评价表获取评分
            List<Map<String, Object>> evaluations = baseMapper.getTeacherEvaluations(assignmentIds);

            if (!evaluations.isEmpty()) {
                double totalScore = evaluations.stream()
                        .mapToDouble(e -> ((Number) e.get("score")).doubleValue())
                        .sum();
                double averageScore = totalScore / evaluations.size();
                stats.put("averageScore", averageScore);
            } else {
                stats.put("averageScore", 0.0);
            }
        } else {
            stats.put("averageScore", 0.0);
        }

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
        record.setType(InvigilationRecordType.NOTE);
        record.setContent("巡查结果：" + inspectionDetails.toString());
        record.setCreateTime(LocalDateTime.now());

        boolean success = save(record);
        log.info("记录巡查结果{}，监考安排ID：{}", success ? "成功" : "失败", assignmentId);
        return success;
    }

    @Override
    public List<InvigilationRecord> getPendingExceptionEvents() {
        log.info("获取未处理的异常事件");
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT)
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
                .eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT) // 2表示异常事件
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

    @Override
    public List<Map<String, Object>> getTeacherInvigilationHistory(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取教师监考历史失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 获取该教师的所有监考安排
        List<InvigilatorAssignment> assignments = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .orderByDesc(InvigilatorAssignment::getExamStart)
                .list();

        List<Map<String, Object>> history = new ArrayList<>();
        for (InvigilatorAssignment assignment : assignments) {
            Map<String, Object> record = new HashMap<>();
            record.put("assignmentId", assignment.getAssignmentId());
            record.put("examTime", assignment.getExamStart());
            record.put("courseName", assignment.getCourseName());
            record.put("location", assignment.getLocation());
            record.put("status", assignment.getStatus());

            // 获取评价信息
            Map<String, Object> evaluation = baseMapper.getTeacherEvaluations(List.of(assignment.getAssignmentId()))
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (evaluation != null) {
                record.put("evaluation", evaluation);
            }

            // 获取该监考安排的记录
            List<InvigilationRecord> records = lambdaQuery()
                    .eq(InvigilationRecord::getAssignmentId, assignment.getAssignmentId())
                    .orderByDesc(InvigilationRecord::getCreateTime)
                    .list();

            record.put("records", records);
            history.add(record);
        }

        log.info("成功获取教师监考历史，教师ID：{}，历史记录数：{}", teacherId, history.size());
        return history;
    }
    
    @Override
    public List<Map<String, Object>> getTeacherInvigilationHistoryByYear(Integer teacherId, int year) {
        if (teacherId == null) {
            log.error("获取教师监考历史失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        
        // 设置指定年份的时间范围
        LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59);

        // 获取该教师在指定年份的所有监考安排
        List<InvigilatorAssignment> assignments = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamStart, endTime)
                .orderByDesc(InvigilatorAssignment::getExamStart)
                .list();

        List<Map<String, Object>> history = new ArrayList<>();
        for (InvigilatorAssignment assignment : assignments) {
            Map<String, Object> record = new HashMap<>();
            record.put("assignmentId", assignment.getAssignmentId());
            record.put("examTime", assignment.getExamStart());
            record.put("courseName", assignment.getCourseName());
            record.put("location", assignment.getLocation());
            record.put("status", assignment.getStatus());

            // 获取评价信息
            Map<String, Object> evaluation = baseMapper.getTeacherEvaluations(List.of(assignment.getAssignmentId()))
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (evaluation != null) {
                record.put("evaluation", evaluation);
            }

            // 获取该监考安排的记录
            List<InvigilationRecord> records = lambdaQuery()
                    .eq(InvigilationRecord::getAssignmentId, assignment.getAssignmentId())
                    .orderByDesc(InvigilationRecord::getCreateTime)
                    .list();

            record.put("records", records);
            history.add(record);
        }

        log.info("成功获取教师{}年份的监考历史，教师ID：{}，历史记录数：{}", year, teacherId, history.size());
        return history;
    }

    @Override
    public Map<String, Object> getDashboardData() {
        try {
            Map<String, Object> dashboardData = new HashMap<>();

            // 获取当前时间和一年前的时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneYearAgo = now.minusYears(1);

            // 获取总体监考统计信息
            Map<String, Object> statistics = getInvigilationStatistics(oneYearAgo, now);
            dashboardData.put("statistics", statistics);

            // 获取最近的监考记录（最近10条）
            LambdaQueryWrapper<InvigilationRecord> recentWrapper = new LambdaQueryWrapper<>();
            recentWrapper.orderByDesc(InvigilationRecord::getCreateTime)
                    .last("LIMIT 10");
            List<InvigilationRecord> recentRecords = list(recentWrapper);
            dashboardData.put("recentRecords", recentRecords);

            // 获取异常事件统计
            LambdaQueryWrapper<InvigilationRecord> exceptionWrapper = new LambdaQueryWrapper<>();
            exceptionWrapper.eq(InvigilationRecord::getType, InvigilationRecordType.INCIDENT)
                    .between(InvigilationRecord::getCreateTime, oneYearAgo, now);
            long totalExceptions = count(exceptionWrapper);
            dashboardData.put("totalExceptions", totalExceptions);

            log.info("成功获取监考管理仪表盘数据");
            return dashboardData;
        } catch (Exception e) {
            log.error("获取监考管理仪表盘数据失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "获取监考管理仪表盘数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTeacherInvigilationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取教师监考统计信息，时间范围：{} - {}", startTime, endTime);
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 获取所有监考安排
            List<InvigilatorAssignment> assignments = assignmentService.getByTimeRange(startTime, endTime);
            log.debug("查询到{}条监考安排记录", assignments != null ? assignments.size() : 0);
            
            if (assignments == null || assignments.isEmpty()) {
                log.warn("指定时间范围内没有监考安排记录：{} - {}", startTime, endTime);
                // 添加默认数据，避免前端显示"暂无数据"
                statistics.put("teacherStats", new ArrayList<>());
                statistics.put("totalTeachers", 0);
                statistics.put("message", "在指定时间范围内未找到监考记录，请尝试调整查询时间范围");
                return statistics;
            }
            
            // 按教师ID分组统计
            Map<Integer, Long> teacherAssignmentCounts = new HashMap<>();
            Map<Integer, Long> teacherCompletedCounts = new HashMap<>();
            
            // 记录有多少监考安排没有分配教师
            long unassignedCount = 0;
            
            for (InvigilatorAssignment assignment : assignments) {
                Integer teacherId = assignment.getTeacherId();
                if (teacherId != null) {
                    // 统计总监考次数
                    teacherAssignmentCounts.merge(teacherId, 1L, Long::sum);
                    
                    // 统计已完成监考次数
                    if (assignment.getStatus() == InvigilatorAssignmentStatus.COMPLETED) {
                        teacherCompletedCounts.merge(teacherId, 1L, Long::sum);
                    }
                } else {
                    unassignedCount++;
                }
            }
            
            log.debug("找到{}位教师有监考安排，{}个监考安排未分配教师", 
                    teacherAssignmentCounts.size(), unassignedCount);
            
            // 构建教师监考统计列表
            List<Map<String, Object>> teacherStats = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : teacherAssignmentCounts.entrySet()) {
                Integer teacherId = entry.getKey();
                Long totalCount = entry.getValue();
                Long completedCount = teacherCompletedCounts.getOrDefault(teacherId, 0L);
                
                Map<String, Object> teacherStat = new HashMap<>();
                teacherStat.put("teacherId", teacherId);
                teacherStat.put("totalCount", totalCount);
                teacherStat.put("completedCount", completedCount);
                teacherStat.put("completionRate", totalCount > 0 ? (completedCount * 100.0 / totalCount) : 0);
                
                // 获取教师姓名和部门信息
                Teacher teacher = teacherService.getById(teacherId);
                if (teacher != null) {
                    teacherStat.put("teacherName", teacher.getName());
                    teacherStat.put("department", teacher.getDepartment());
                } else {
                    log.warn("找不到ID为{}的教师信息", teacherId);
                    teacherStat.put("teacherName", "未知");
                    teacherStat.put("department", "未知");
                }
                
                teacherStats.add(teacherStat);
            }
            
            // 按监考次数降序排序
            teacherStats.sort((a, b) -> {
                Long countA = (Long) a.get("totalCount");
                Long countB = (Long) b.get("totalCount");
                return countB.compareTo(countA);
            });
            
            statistics.put("teacherStats", teacherStats);
            statistics.put("totalTeachers", teacherStats.size());
            
            if (teacherStats.isEmpty()) {
                statistics.put("message", "在指定时间范围内未找到教师监考记录，请尝试调整查询时间范围");
            }
            
            log.info("成功获取教师监考统计信息，共{}位教师", teacherStats.size());
        } catch (Exception e) {
            log.error("获取教师监考统计信息失败", e);
            statistics.put("teacherStats", new ArrayList<>());
            statistics.put("totalTeachers", 0);
            statistics.put("message", "获取教师监考统计信息失败：" + e.getMessage());
        }

        return statistics;
    }
}