package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    public List<InvigilatorAssignment> getTeacherAssignmentsByTimeRange(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("获取教师监考任务，教师ID：{}，时间范围：{} - {}", teacherId, startDate, endDate);
        return lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startDate)
                .le(InvigilatorAssignment::getExamEnd, endDate)
                .orderByAsc(InvigilatorAssignment::getExamStart)
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
    public boolean updateStatus(Long assignmentId, InvigilatorAssignmentStatus status) {
        if (assignmentId == null || status == null) {
            log.error("更新监考安排状态失败：参数为空");
            return false;
        }

        InvigilatorAssignment assignment = getById(assignmentId);
        if (assignment == null) {
            log.error("更新监考安排状态失败：监考安排不存在，ID={}", assignmentId);
            return false;
        }

        assignment.setStatus(status);
        assignment.setConfirmTime(LocalDateTime.now());
        return updateById(assignment);
    }

    @Override
    @Transactional
    public boolean cancelAssignment(Long assignmentId) {
        if (assignmentId == null) {
            log.error("取消监考安排失败：监考安排ID为空");
            return false;
        }

        InvigilatorAssignment assignment = getById(assignmentId);
        if (assignment == null) {
            log.error("取消监考安排失败：监考安排不存在，ID={}", assignmentId);
            return false;
        }

        if (assignment.getStatus() == InvigilatorAssignmentStatus.COMPLETED) {
            log.error("取消监考安排失败：监考已完成，不能取消，ID={}", assignmentId);
            return false;
        }

        assignment.setStatus(InvigilatorAssignmentStatus.CANCELLED);
        return updateById(assignment);
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
        assignment.setStatus(InvigilatorAssignmentStatus.PENDING);
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
                .ne(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.CANCELLED) // 排除已取消的安排
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
                .set("status", InvigilatorAssignmentStatus.CONFIRMED.getValue())
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

    @Override
    public List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId, InvigilatorAssignmentStatus status,
            String startDate, String endDate) {
        // 先执行一次状态更新
        autoUpdateAssignmentStatus();

        // 构建查询条件
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, teacherId);

        if (status != null) {
            wrapper.eq(InvigilatorAssignment::getStatus, status);
        }

        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(InvigilatorAssignment::getExamStart,
                    LocalDateTime.parse(startDate + "T00:00:00"),
                    LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(InvigilatorAssignment::getExamStart);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void confirmAssignment(Long assignmentId, Integer teacherId) {
        try {
            log.debug("教师{}确认监考任务{}", teacherId, assignmentId);

            InvigilatorAssignment assignment = getById(assignmentId);
            if (assignment == null) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
            }

            if (!assignment.getTeacherId().equals(teacherId)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此监考任务");
            }

            if (assignment.getStatus() != InvigilatorAssignmentStatus.PENDING) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_CONFIRMED);
            }

            assignment.setStatus(InvigilatorAssignmentStatus.CONFIRMED);
            assignment.setConfirmTime(LocalDateTime.now());
            updateById(assignment);

            log.info("教师{}成功确认监考任务{}", teacherId, assignmentId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("确认监考任务时发生错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "确认监考任务失败");
        }
    }

    @Override
    @Transactional
    public void cancelAssignment(Long assignmentId, Integer teacherId) {
        try {
            log.debug("教师{}取消监考任务{}", teacherId, assignmentId);

            InvigilatorAssignment assignment = getById(assignmentId);
            if (assignment == null) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
            }

            if (!assignment.getTeacherId().equals(teacherId)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此监考任务");
            }

            if (assignment.getStatus() != InvigilatorAssignmentStatus.PENDING) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_CANCELED);
            }

            assignment.setStatus(InvigilatorAssignmentStatus.CANCELLED);
            updateById(assignment);

            log.info("教师{}成功取消监考任务{}", teacherId, assignmentId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消监考任务时发生错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取消监考任务失败");
        }
    }

    @Override
    public List<InvigilatorAssignment> getTeacherAssignmentsByDateRange(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startDate)
                .le(InvigilatorAssignment::getExamEnd, endDate)
                .orderByDesc(InvigilatorAssignment::getExamStart)
                .list();
    }

    @Override
    public Map<String, Object> getAssignmentStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 获取时间范围内的所有监考任务
        List<InvigilatorAssignment> assignments = lambdaQuery()
                .ge(InvigilatorAssignment::getExamStart, startDate)
                .le(InvigilatorAssignment::getExamEnd, endDate)
                .list();

        // 统计各种状态的数量
        long totalCount = assignments.size();
        long pendingCount = assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.PENDING)
                .count();
        long confirmedCount = assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.CONFIRMED)
                .count();
        long completedCount = assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED)
                .count();

        stats.put("totalCount", totalCount);
        stats.put("pendingCount", pendingCount);
        stats.put("confirmedCount", confirmedCount);
        stats.put("completedCount", completedCount);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);

        return stats;
    }

    @Override
    public Map<String, Object> getTeacherWorkloadStats(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 获取教师在指定时间范围内的监考任务
        List<InvigilatorAssignment> assignments = getTeacherAssignmentsByTimeRange(teacherId, startDate, endDate);

        // 统计各种状态的监考数量
        stats.put("totalCount", assignments.size());
        stats.put("pendingCount", assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.PENDING)
                .count());
        stats.put("completedCount", assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED)
                .count());
        stats.put("canceledCount", assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.CANCELLED)
                .count());

        return stats;
    }

    /**
     * 每5分钟执行一次状态更新
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void autoUpdateAssignmentStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 更新已确认且已结束的监考为已完成
        LambdaUpdateWrapper<InvigilatorAssignment> completedWrapper = new LambdaUpdateWrapper<>();
        completedWrapper.eq(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.CONFIRMED)
                .lt(InvigilatorAssignment::getExamEnd, now)
                .set(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.COMPLETED);
        update(null, completedWrapper);

        // 更新未确认且已过期的监考为已取消
        LambdaUpdateWrapper<InvigilatorAssignment> cancelledWrapper = new LambdaUpdateWrapper<>();
        cancelledWrapper.eq(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.PENDING)
                .lt(InvigilatorAssignment::getExamStart, now)
                .set(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.CANCELLED);
        update(null, cancelledWrapper);

        log.info("已完成监考状态的自动更新");
    }
}