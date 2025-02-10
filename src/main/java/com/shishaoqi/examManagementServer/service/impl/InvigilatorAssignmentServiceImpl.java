package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class InvigilatorAssignmentServiceImpl extends ServiceImpl<InvigilatorAssignmentMapper, InvigilatorAssignment>
        implements InvigilatorAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(InvigilatorAssignmentServiceImpl.class);

    @Override
    public List<InvigilatorAssignment> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .list();
    }

    @Override
    public List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId) {
        return lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .orderByDesc(InvigilatorAssignment::getExamStart)
                .list();
    }

    @Override
    public List<InvigilatorAssignment> getTeacherAssignmentsByTimeRange(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime) {
        return lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .orderByDesc(InvigilatorAssignment::getExamStart)
                .list();
    }

    @Override
    public List<InvigilatorAssignment> getAssignmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .orderByAsc(InvigilatorAssignment::getExamStart)
                .list();
    }

    @Override
    @Transactional
    public boolean updateStatus(Long assignmentId, Integer status) {
        if (assignmentId == null || status == null) {
            log.error("更新监考安排状态失败：参数为空，安排ID：{}，状态：{}", assignmentId, status);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("开始更新监考安排状态，安排ID：{}，状态：{}", assignmentId, status);
        InvigilatorAssignment assignment = getById(assignmentId);
        if (assignment == null) {
            log.error("监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        if (status < 0 || status > 2) {
            log.error("无效的状态值：{}", status);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的状态值");
        }

        if (assignment.getStatus() == 2) {
            log.error("监考安排已取消，无法更新状态，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_CANCELED);
        }

        // 检查是否已经确认
        if (assignment.getStatus() == 1 && status == 1) {
            log.error("监考安排已确认，无需重复确认，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_CONFIRMED);
        }

        assignment.setStatus(status);
        assignment.setConfirmTime(LocalDateTime.now());
        boolean success = updateById(assignment);
        log.info("更新监考安排状态{}，安排ID：{}", success ? "成功" : "失败", assignmentId);
        return success;
    }

    @Override
    @Transactional
    public boolean cancelAssignment(Long assignmentId) {
        if (assignmentId == null) {
            log.error("取消监考安排失败：安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("开始取消监考安排，安排ID：{}", assignmentId);
        InvigilatorAssignment assignment = getById(assignmentId);
        if (assignment == null) {
            log.error("监考安排不存在，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        if (assignment.getStatus() == 2) {
            log.error("监考安排已经取消，安排ID：{}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_CANCELED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (assignment.getExamStart().isBefore(now)) {
            log.error("监考已开始，无法取消，安排ID：{}，开始时间：{}", assignmentId, assignment.getExamStart());
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "监考已开始，无法取消");
        }

        assignment.setStatus(2);
        assignment.setConfirmTime(now);
        boolean success = updateById(assignment);
        log.info("取消监考安排{}，安排ID：{}", success ? "成功" : "失败", assignmentId);
        return success;
    }

    @Override
    @Transactional
    public boolean save(InvigilatorAssignment assignment) {
        if (assignment == null) {
            log.error("创建监考安排失败：参数为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证必要字段
        validateAssignment(assignment);

        // 检查时间冲突
        if (hasTimeConflict(assignment)) {
            log.error("创建监考安排失败：时间冲突，教师ID：{}，开始时间：{}，结束时间：{}",
                    assignment.getTeacherId(), assignment.getExamStart(), assignment.getExamEnd());
            throw new BusinessException(ErrorCode.ASSIGNMENT_TIME_CONFLICT);
        }

        // 设置初始值
        assignment.setStatus(0);
        assignment.setAssignTime(LocalDateTime.now());

        boolean success = super.save(assignment);
        log.info("创建监考安排{}，教师ID：{}", success ? "成功" : "失败", assignment.getTeacherId());
        return success;
    }

    private void validateAssignment(InvigilatorAssignment assignment) {
        if (assignment.getTeacherId() == null) {
            log.error("监考安排验证失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "教师ID不能为空");
        }
        if (assignment.getExamStart() == null) {
            log.error("监考安排验证失败：开始时间为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能为空");
        }
        if (assignment.getExamEnd() == null) {
            log.error("监考安排验证失败：结束时间为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "结束时间不能为空");
        }
        if (assignment.getExamStart().isAfter(assignment.getExamEnd())) {
            log.error("监考安排验证失败：开始时间晚于结束时间");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能晚于结束时间");
        }
        if (assignment.getExamStart().isBefore(LocalDateTime.now())) {
            log.error("监考安排验证失败：开始时间早于当前时间");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能早于当前时间");
        }
        if (assignment.getCourseName() == null || assignment.getCourseName().trim().isEmpty()) {
            log.error("监考安排验证失败：课程名称为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "课程名称不能为空");
        }
        if (assignment.getLocation() == null || assignment.getLocation().trim().isEmpty()) {
            log.error("监考安排验证失败：考试地点为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "考试地点不能为空");
        }
    }

    private boolean hasTimeConflict(InvigilatorAssignment newAssignment) {
        if (newAssignment.getExamStart() == null || newAssignment.getExamEnd() == null) {
            return false;
        }

        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, newAssignment.getTeacherId())
                .ne(InvigilatorAssignment::getStatus, 2) // 排除已取消的安排
                .and(w -> w
                        .between(InvigilatorAssignment::getExamStart, newAssignment.getExamStart(),
                                newAssignment.getExamEnd())
                        .or()
                        .between(InvigilatorAssignment::getExamEnd, newAssignment.getExamStart(),
                                newAssignment.getExamEnd()));

        long count = count(wrapper);
        if (count > 0) {
            log.warn("检测到时间冲突，教师ID：{}，开始时间：{}，结束时间：{}，冲突数量：{}",
                    newAssignment.getTeacherId(), newAssignment.getExamStart(), newAssignment.getExamEnd(), count);
        }
        return count > 0;
    }

    @Override
    @Transactional
    public boolean batchConfirmAssignments(List<Long> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return update().in("assignment_id", assignmentIds)
                .set("status", 1)
                .update();
    }

    @Override
    public List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime) {
        if (teacherId == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .between(InvigilatorAssignment::getExamStart, startTime, endTime)
                .orderByAsc(InvigilatorAssignment::getExamStart)
                .list();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> autoAssignInvigilators(Long examId, int count) {
        if (examId == null || count <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 自动分配监考老师的逻辑需要根据具体业务规则实现
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getWorkloadBalanceReport(String timeRange) {
        Map<String, Object> report = new HashMap<>();
        // 工作量平衡报告的具体实现需要根据业务规则来完成
        return report;
    }

    @Override
    public List<Teacher> matchInvigilatorsBySpecialty(String subjectArea, int count) {
        if (subjectArea == null || count <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 根据专业匹配监考教师的逻辑需要根据具体业务规则实现
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getConflictReport(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 时间冲突报告的具体实现需要根据业务规则来完成
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public boolean batchAdjustAssignments(List<Map<String, Object>> adjustments) {
        if (adjustments == null || adjustments.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 批量调整监考安排的逻辑需要根据具体业务规则实现
        return true;
    }

    @Override
    @Transactional
    public Map<String, Object> resolveTimeConflict(InvigilatorAssignment assignment) {
        if (assignment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 解决时间冲突的逻辑需要根据具体业务规则实现
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> generateAssignmentSuggestions(Long examId) {
        if (examId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 生成监考安排建议的逻辑需要根据具体业务规则实现
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getExamAssignmentStatistics(Long examId) {
        if (examId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Map<String, Object> statistics = new HashMap<>();
        // 考试监考安排统计的具体实现需要根据业务规则来完成
        return statistics;
    }
}