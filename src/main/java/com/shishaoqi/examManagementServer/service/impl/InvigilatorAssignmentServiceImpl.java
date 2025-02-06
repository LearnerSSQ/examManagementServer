package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
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

@Service
public class InvigilatorAssignmentServiceImpl extends ServiceImpl<InvigilatorAssignmentMapper, InvigilatorAssignment>
        implements InvigilatorAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(InvigilatorAssignmentServiceImpl.class);

    @Override
    public List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取教师监考安排失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("获取教师监考安排列表，教师ID：{}", teacherId);
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, teacherId)
                .orderByDesc(InvigilatorAssignment::getExamStart);

        List<InvigilatorAssignment> assignments = list(wrapper);
        log.info("获取到{}条监考安排，教师ID：{}", assignments.size(), teacherId);
        return assignments;
    }

    @Override
    public List<InvigilatorAssignment> getAssignmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            log.error("获取时间段内监考安排失败：时间参数为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (startTime.isAfter(endTime)) {
            log.error("获取时间段内监考安排失败：开始时间[{}]晚于结束时间[{}]", startTime, endTime);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能晚于结束时间");
        }

        log.info("获取时间段内的监考安排，开始时间：{}，结束时间：{}", startTime, endTime);
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InvigilatorAssignment::getExamStart, startTime, endTime)
                .orderByAsc(InvigilatorAssignment::getExamStart);

        List<InvigilatorAssignment> assignments = list(wrapper);
        log.info("获取到{}条监考安排", assignments.size());
        return assignments;
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
}