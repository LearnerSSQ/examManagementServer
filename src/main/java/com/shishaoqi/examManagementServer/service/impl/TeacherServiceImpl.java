package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import com.shishaoqi.examManagementServer.service.*;
import com.shishaoqi.examManagementServer.common.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final EvaluationService evaluationService;
    private final InvigilatorAssignmentService assignmentService;
    private final TrainingRecordService trainingRecordService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public TeacherServiceImpl(
            EvaluationService evaluationService,
            InvigilatorAssignmentService assignmentService,
            TrainingRecordService trainingRecordService,
            BCryptPasswordEncoder passwordEncoder) {
        this.evaluationService = evaluationService;
        this.assignmentService = assignmentService;
        this.trainingRecordService = trainingRecordService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public boolean addTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "教师信息不能为空");
        }
        // 检查邮箱是否已存在
        if (baseMapper.selectByEmail(teacher.getEmail()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已存在");
        }
        // 检查手机号是否已存在
        if (baseMapper.selectByPhone(teacher.getPhone()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "手机号已存在");
        }
        // 设置默认角色
        teacher.setRole(TeacherRole.TEACHER);
        // 加密密码
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        // 设置默认状态为已激活
        teacher.setStatus(TeacherStatus.ACTIVE);
        teacher.setCreateTime(LocalDateTime.now());
        return baseMapper.insert(teacher) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCache", key = "#teacher.teacherId")
    public boolean updateTeacher(Teacher teacher) {
        if (teacher == null || teacher.getTeacherId() == null) {
            log.error("更新教师信息失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return updateById(teacher);
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCache", allEntries = true)
    public boolean batchUpdateTeachers(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            log.error("批量更新教师信息失败：教师列表为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return updateBatchById(teachers);
    }

    @Override
    @Cacheable(value = "teacherCache", key = "#teacherId")
    public Teacher getTeacherById(Integer teacherId) {
        return baseMapper.selectById(teacherId);
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'dept:' + #department")
    public List<Teacher> getTeachersByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            log.error("获取部门教师列表失败：部门名称为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("获取部门教师列表，部门：{}", department);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getDepartment, department)
                .orderByAsc(Teacher::getTeacherId);
        List<Teacher> teachers = list(wrapper);
        log.info("获取到{}名教师，部门：{}", teachers.size(), department);
        return teachers;
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.error("根据邮箱查询教师失败：邮箱为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("根据邮箱查询教师，邮箱：{}", email);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getEmail, email);
        Teacher teacher = getOne(wrapper);
        if (teacher == null) {
            log.warn("未找到教师，邮箱：{}", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return teacher;
    }

    @Override
    public Teacher getTeacherByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            log.error("根据手机号查询教师失败：手机号为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("根据手机号查询教师，手机号：{}", phone);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getPhone, phone);
        Teacher teacher = getOne(wrapper);
        if (teacher == null) {
            log.warn("未找到教师，手机号：{}", phone);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return teacher;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCache", key = "#teacherId")
    public boolean updateStatus(Integer teacherId, TeacherStatus status) {
        if (teacherId == null || status == null) {
            log.error("更新教师状态失败：参数为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.error("更新教师状态失败：教师不存在，ID={}", teacherId);
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        teacher.setStatus(status);
        return updateById(teacher);
    }

    @Override
    @Transactional
    public boolean updateLastLogin(Integer teacherId) {
        if (teacherId == null) {
            log.error("更新最后登录时间失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.error("更新最后登录时间失败：教师不存在，ID={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (teacher.getStatus() == TeacherStatus.INACTIVE) {
            log.error("更新最后登录时间失败：教师账号已禁用，ID={}", teacherId);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        boolean success = baseMapper.updateLastLogin(teacherId, LocalDateTime.now()) > 0;
        if (success) {
            log.info("成功更新教师最后登录时间，教师ID：{}", teacherId);
        }
        return success;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCache", key = "#teacherId")
    public boolean updateTitle(Integer teacherId, String title) {
        if (teacherId == null || title == null || title.trim().isEmpty()) {
            log.error("更新教师职称失败：参数无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.error("更新教师职称失败：教师不存在，ID={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (teacher.getStatus() == TeacherStatus.INACTIVE) {
            log.error("更新教师职称失败：教师账号已禁用，ID={}", teacherId);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        boolean success = baseMapper.updateTitle(teacherId, title.trim()) > 0;
        if (success) {
            log.info("成功更新教师职称，教师ID：{}，新职称：{}", teacherId, title);
        } else {
            log.error("更新教师职称失败，教师ID：{}", teacherId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return success;
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'available:' + #minScore + ':' + #maxAssignments")
    public List<Map<String, Object>> getAvailableInvigilators(Double minScore, Integer maxAssignments) {
        log.info("获取可用监考教师列表，最低评分要求：{}，最大监考次数：{}", minScore, maxAssignments);
        List<Map<String, Object>> availableTeachers = new ArrayList<>();

        // 获取所有激活状态的教师
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getStatus, TeacherStatus.ACTIVE);

        List<Teacher> allTeachers = list(wrapper);

        // 过滤出评分达标且具有监考资格的教师
        for (Teacher teacher : allTeachers) {
            String title = teacher.getTitle();
            if (title == null || !title.contains("[专业:") || !title.contains("(监考资格)")) {
                continue;
            }

            BigDecimal avgScore = evaluationService.getTeacherAverageScore(teacher.getTeacherId());
            if (avgScore == null || avgScore.doubleValue() < minScore) {
                log.debug("教师{}的评分（{}）未达到要求（{}）",
                        teacher.getName(), avgScore, minScore);
                continue;
            }

            // 检查是否有未完成或待确认的监考任务
            List<InvigilatorAssignment> assignments = assignmentService
                    .getTeacherAssignments(teacher.getTeacherId());

            long pendingCount = assignments.stream()
                    .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.PENDING ||
                            (a.getStatus() == InvigilatorAssignmentStatus.CONFIRMED &&
                                    a.getExamStart().isAfter(LocalDateTime.now())))
                    .count();

            if (pendingCount >= maxAssignments) {
                log.debug("教师{}当前有{}个待处理的监考任务，暂不分配新任务",
                        teacher.getName(), pendingCount);
                continue;
            }

            Map<String, Object> teacherInfo = new HashMap<>();
            teacherInfo.put("teacherId", teacher.getTeacherId());
            teacherInfo.put("name", teacher.getName());
            teacherInfo.put("department", teacher.getDepartment());
            teacherInfo.put("title", teacher.getTitle());
            teacherInfo.put("averageScore", avgScore);
            availableTeachers.add(teacherInfo);
        }

        log.info("找到{}名可用的监考教师", availableTeachers.size());
        return availableTeachers;
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'training:' + #department")
    public List<Map<String, Object>> getTeacherTrainingStatus(String department) {
        log.info("获取教师培训状态，部门：{}", department);
        return baseMapper.getTeacherTrainingStatus(department);
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'experience:' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getTeacherExperienceStats(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取教师经验统计，时间范围：{} 至 {}", startDate, endDate);
        return baseMapper.getTeacherExperienceStats(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> checkTimeConflicts(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("检查教师时间冲突，教师ID：{}，开始时间：{}，结束时间：{}", teacherId, startDate, endDate);
        if (teacherId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LocalDateTime extendedStart = startDate.minusHours(24);
        LocalDateTime extendedEnd = endDate.plusHours(24);
        List<InvigilatorAssignment> assignments = assignmentService.getAssignmentsByTimeRange(extendedStart,
                extendedEnd);

        List<Map<String, Object>> conflicts = new ArrayList<>();
        assignments.stream()
                .filter(a -> a.getTeacherId().equals(teacherId)
                        && a.getStatus() != InvigilatorAssignmentStatus.CANCELLED)
                .forEach(assignment -> {
                    boolean timeOverlap = !(endDate.isBefore(assignment.getExamStart()) ||
                            startDate.isAfter(assignment.getExamEnd()));
                    boolean inBuffer = !timeOverlap &&
                            startDate.minusHours(24).isBefore(assignment.getExamEnd()) &&
                            endDate.plusHours(24).isAfter(assignment.getExamStart());

                    if (timeOverlap || inBuffer) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("assignmentId", assignment.getAssignmentId());
                        conflict.put("examStart", assignment.getExamStart());
                        conflict.put("examEnd", assignment.getExamEnd());
                        conflict.put("location", assignment.getLocation());
                        conflict.put("type", timeOverlap ? "直接冲突" : "缓冲期冲突");
                        conflicts.add(conflict);
                    }
                });

        return conflicts;
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'deptWorkload'")
    public List<Map<String, Object>> getDepartmentWorkloadStats() {
        log.info("获取部门工作量统计");

        // 获取所有教师
        List<Teacher> allTeachers = this.list();

        // 按部门分组统计
        Map<String, List<Teacher>> teachersByDepartment = allTeachers.stream()
                .collect(Collectors.groupingBy(Teacher::getDepartment));

        // 计算每个部门的统计信息
        return teachersByDepartment.entrySet().stream()
                .map(entry -> {
                    String department = entry.getKey();
                    List<Teacher> teachers = entry.getValue();

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("department", department);
                    stats.put("totalTeachers", teachers.size());

                    // 计算激活状态的教师数量
                    long activeTeachers = teachers.stream()
                            .filter(t -> t.getStatus() == TeacherStatus.ACTIVE)
                            .count();
                    stats.put("activeTeachers", activeTeachers);

                    // 按职称统计
                    Map<String, Long> titleStats = teachers.stream()
                            .collect(Collectors.groupingBy(
                                    Teacher::getTitle,
                                    Collectors.counting()));
                    stats.put("titleDistribution", titleStats);

                    return stats;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTeacherWorkloadStats(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("获取教师{}的工作量统计，时间范围：{} - {}", teacherId, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        try {
            // 1. 获取监考次数（只统计已完成的监考）
            LambdaQueryWrapper<InvigilatorAssignment> assignmentWrapper = new LambdaQueryWrapper<>();
            assignmentWrapper.eq(InvigilatorAssignment::getTeacherId, teacherId)
                    .eq(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.COMPLETED);
            long invigilationCount = assignmentService.count(assignmentWrapper);
            stats.put("invigilationCount", invigilationCount);

            // 2. 获取平均评分
            BigDecimal avgScore = evaluationService.getTeacherAverageScore(teacherId);
            stats.put("averageScore", avgScore != null ? avgScore.doubleValue() : 0.0);

            // 3. 计算培训完成率
            LambdaQueryWrapper<TrainingRecord> trainingWrapper = new LambdaQueryWrapper<>();
            trainingWrapper.eq(TrainingRecord::getTeacherId, teacherId)
                    .eq(TrainingRecord::getStatus, TrainingRecordStatus.COMPLETED);
            long completedTrainings = trainingRecordService.count(trainingWrapper);

            trainingWrapper.clear();
            trainingWrapper.eq(TrainingRecord::getTeacherId, teacherId)
                    .in(TrainingRecord::getStatus,
                            Arrays.asList(TrainingRecordStatus.NOT_STARTED,
                                    TrainingRecordStatus.IN_PROGRESS,
                                    TrainingRecordStatus.COMPLETED));
            long totalTrainings = trainingRecordService.count(trainingWrapper);

            double completionRate = totalTrainings > 0 ? (completedTrainings * 100.0 / totalTrainings) : 0;
            stats.put("trainingCompletionRate", completionRate);

            // 4. 获取监考历史（最近的监考记录）
            List<Map<String, Object>> history = getInvigilationHistory(teacherId);
            stats.put("invigilationHistory", history);

            log.info("成功获取教师{}的统计数据：监考次数={}，平均评分={}，培训完成率={}%", teacherId, invigilationCount, stats.get("averageScore"),
                    completionRate);

        } catch (Exception e) {
            log.error("获取教师{}的统计数据失败", teacherId, e);
            // 设置默认值
            stats.put("invigilationCount", 0);
            stats.put("averageScore", 0.0);
            stats.put("trainingCompletionRate", 0.0);
            stats.put("invigilationHistory", new ArrayList<>());
        }

        return stats;
    }

    @Override
    public boolean checkInvigilationQualification(Integer teacherId) {
        // 检查培训完成情况
        Map<String, Object> trainingCompletion = getTeacherTrainingCompletion(teacherId);
        boolean trainingQualified = (boolean) trainingCompletion.get("isQualified");

        // 检查评分是否达标
        BigDecimal averageScore = evaluationService.getTeacherAverageScore(teacherId);
        boolean scoreQualified = averageScore != null && averageScore.compareTo(new BigDecimal("80")) >= 0;

        // 检查教师状态
        Teacher teacher = getTeacherById(teacherId);
        boolean statusValid = teacher != null && teacher.getStatus() == TeacherStatus.ACTIVE;

        return trainingQualified && scoreQualified && statusValid;
    }

    @Override
    public Map<String, Object> generateTeacherReport(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("生成教师报告，教师ID：{}，时间范围：{} 至 {}", teacherId, startDate, endDate);
        Map<String, Object> report = new HashMap<>();

        // 基本信息
        Teacher teacher = getTeacherById(teacherId);
        report.put("basicInfo", teacher);

        // 工作量统计
        Map<String, Object> workloadStats = getTeacherWorkloadStats(teacherId, startDate, endDate);
        report.put("workloadStats", workloadStats);

        // 评分情况
        Map<String, Object> evaluationReport = evaluationService.generateTeacherEvaluationReport(teacherId, startDate,
                endDate);
        report.put("evaluationReport", evaluationReport);

        // 培训情况
        Map<String, Object> trainingCompletion = getTeacherTrainingCompletion(teacherId);
        report.put("trainingCompletion", trainingCompletion);

        return report;
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'ranking:' + #department")
    public List<Map<String, Object>> getDepartmentTeacherRanking(String department) {
        log.info("获取部门教师排名，部门：{}", department);
        List<Teacher> teachers = getTeachersByDepartment(department);

        return teachers.stream()
                .map(teacher -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("teacherId", teacher.getTeacherId());
                    stats.put("name", teacher.getName());
                    stats.put("department", teacher.getDepartment());
                    stats.put("averageScore", evaluationService.getTeacherAverageScore(teacher.getTeacherId()));
                    return stats;
                })
                .filter(stats -> stats.get("averageScore") != null)
                .sorted((a, b) -> {
                    BigDecimal scoreA = (BigDecimal) a.get("averageScore");
                    BigDecimal scoreB = (BigDecimal) b.get("averageScore");
                    return scoreB.compareTo(scoreA);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'trainingCompletion:' + #teacherId")
    public Map<String, Object> getTeacherTrainingCompletion(Integer teacherId) {
        try {
            log.debug("获取教师{}的培训完成情况", teacherId);
            Map<String, Object> stats = new HashMap<>();

            // 暂时返回默认值
            stats.put("completedCount", 0);
            stats.put("totalCount", 0);
            stats.put("completionRate", 0);

            return stats;
        } catch (Exception e) {
            log.error("获取教师{}的培训完成情况失败", teacherId, e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public boolean batchUpdateQualification(List<Integer> teacherIds, boolean qualified) {
        log.info("批量更新教师监考资格，教师数量：{}，是否具有资格：{}", teacherIds.size(), qualified);
        TeacherStatus status = qualified ? TeacherStatus.ACTIVE : TeacherStatus.INACTIVE;
        return teacherIds.stream()
                .map(id -> baseMapper.updateStatus(id, status))
                .allMatch(result -> result > 0);
    }

    @Override
    @Transactional
    public boolean updateSpecialties(Integer teacherId, List<String> specialties) {
        log.info("更新教师专业信息，教师ID：{}，专业数量：{}", teacherId, specialties.size());
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String specialtiesStr = String.join(",", specialties);
        return baseMapper.updateTitle(teacherId, teacher.getTitle() + "[专业:" + specialtiesStr + "]") > 0;
    }

    @Override
    public List<Teacher> getQualifiedInvigilators(double minScore) {
        log.info("获取合格监考教师列表，最低评分要求：{}", minScore);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getStatus, TeacherStatus.ACTIVE);
        List<Teacher> teachers = list(wrapper);

        return teachers.stream()
                .filter(teacher -> {
                    BigDecimal avgScore = evaluationService.getTeacherAverageScore(teacher.getTeacherId());
                    return avgScore != null && avgScore.doubleValue() >= minScore;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getInvigilationStatistics(Integer teacherId) {
        try {
            log.debug("获取教师{}的监考统计信息", teacherId);
            Map<String, Object> stats = new HashMap<>();

            // 获取监考总次数
            Long totalCount = baseMapper.selectCount(
                    new QueryWrapper<Teacher>()
                            .eq("teacher_id", teacherId));
            stats.put("totalCount", totalCount != null ? totalCount : 0);

            // 获取平均评分（暂时返回默认值）
            stats.put("averageScore", 0.0);

            return stats;
        } catch (Exception e) {
            log.error("获取教师{}的监考统计信息失败", teacherId, e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Integer> getWorkloadStatistics(Integer teacherId, int year) {
        log.info("获取教师{}在{}年的工作量统计", teacherId, year);

        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        // 初始化每月的工作量为0
        Map<String, Integer> monthlyStats = new TreeMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyStats.put(String.format("%02d", month), 0);
        }

        // 获取该年度的所有监考任务
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, teacherId)
                .between(InvigilatorAssignment::getExamStart, startOfYear, endOfYear)
                .eq(InvigilatorAssignment::getStatus, InvigilatorAssignmentStatus.COMPLETED);

        List<InvigilatorAssignment> assignments = assignmentService.list(wrapper);

        // 统计每月的监考次数
        assignments.forEach(assignment -> {
            String month = String.format("%02d", assignment.getExamStart().getMonthValue());
            monthlyStats.merge(month, 1, Integer::sum);
        });

        log.info("教师{}在{}年的月度工作量统计：{}", teacherId, year, monthlyStats);
        return monthlyStats;
    }

    @Override
    public boolean checkTimeConflict(Integer teacherId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("检查教师时间冲突，教师ID：{}，开始时间：{}，结束时间：{}", teacherId, startTime, endTime);
        List<InvigilatorAssignment> assignments = assignmentService.getAssignmentsByTimeRange(
                startTime.minusHours(24), endTime.plusHours(24));

        return assignments.stream()
                .anyMatch(a -> a.getTeacherId().equals(teacherId) &&
                        !endTime.isBefore(a.getExamStart()) &&
                        !startTime.isAfter(a.getExamEnd()));
    }

    @Override
    public List<Map<String, Object>> getInvigilationHistory(Integer teacherId) {
        log.info("获取教师{}的监考历史", teacherId);

        // 获取最近一年的监考记录
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, oneYearAgo)
                .orderByDesc(InvigilatorAssignment::getExamStart);

        List<InvigilatorAssignment> assignments = assignmentService.list(wrapper);

        return assignments.stream().map(assignment -> {
            Map<String, Object> history = new HashMap<>();
            history.put("assignmentId", assignment.getAssignmentId());
            history.put("examDate", assignment.getExamStart());
            history.put("status", assignment.getStatus());
            history.put("location", assignment.getLocation());
            history.put("duration", assignment.getExamEnd().toLocalTime().toString() +
                    " - " + assignment.getExamStart().toLocalTime().toString());

            // 获取评价信息
            Map<String, Object> evaluation = evaluationService.getTeacherEvaluationStats(
                    teacherId, assignment.getExamStart(), assignment.getExamEnd());

            if (evaluation != null && evaluation.containsKey("averageScore")) {
                history.put("score", evaluation.get("averageScore"));
            } else {
                history.put("score", "暂无评分");
            }

            return history;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getInvigilationHistory(Integer teacherId,
            LocalDateTime startTime, LocalDateTime endTime, int pageSize, int pageNum) {
        log.info("获取教师监考历史评价，教师ID：{}，时间范围：{} 至 {}，页码：{}，每页数量：{}",
                teacherId, startTime, endTime, pageNum, pageSize);

        List<InvigilatorAssignment> assignments = assignmentService.getAssignmentsByTimeRange(startTime, endTime);
        List<Map<String, Object>> history = new ArrayList<>();

        assignments.stream()
                .filter(a -> a.getTeacherId().equals(teacherId))
                .skip((pageNum - 1) * pageSize)
                .limit(pageSize)
                .forEach(a -> {
                    Map<String, Object> record = new HashMap<>();
                    record.put("assignmentId", a.getAssignmentId());
                    record.put("examStart", a.getExamStart());
                    record.put("location", a.getLocation());
                    record.put("status", a.getStatus());
                    history.add(record);
                });

        return history;
    }

    @Override
    public List<Map<String, Object>> getTopInvigilators(int limit) {
        log.info("获取最佳监考教师排名，限制数量：{}", limit);
        List<Map<String, Object>> topInvigilators = new ArrayList<>();

        // 获取所有激活状态的教师
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getStatus, TeacherStatus.ACTIVE);
        List<Teacher> teachers = list(wrapper);

        // 计算每个教师的综合得分
        teachers.forEach(teacher -> {
            BigDecimal avgScore = evaluationService.getTeacherAverageScore(teacher.getTeacherId());
            if (avgScore != null) {
                Map<String, Object> teacherInfo = new HashMap<>();
                teacherInfo.put("teacherId", teacher.getTeacherId());
                teacherInfo.put("name", teacher.getName());
                teacherInfo.put("department", teacher.getDepartment());
                teacherInfo.put("averageScore", avgScore);
                topInvigilators.add(teacherInfo);
            }
        });

        // 按评分排序并限制数量
        topInvigilators.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("averageScore");
            BigDecimal scoreB = (BigDecimal) b.get("averageScore");
            return scoreB.compareTo(scoreA);
        });

        return topInvigilators.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Teacher login(String email, String password) {
        try {
            log.debug("开始处理登录请求，邮箱：{}", email);

            if (email == null || email.trim().isEmpty() || password == null) {
                log.error("登录失败：邮箱或密码为空");
                throw new BusinessException(ErrorCode.PARAM_ERROR, "邮箱和密码不能为空");
            }

            Teacher teacher = getTeacherByEmail(email);
            if (teacher == null) {
                log.error("登录失败：教师不存在，邮箱：{}", email);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "教师不存在");
            }

            if (teacher.getStatus() == TeacherStatus.DISABLED) {
                log.error("登录失败：教师账号已禁用，邮箱：{}", email);
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
            }

            if (teacher.getStatus() == TeacherStatus.INACTIVE) {
                log.error("登录失败：教师账号未激活，邮箱：{}", email);
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号未激活");
            }

            if (!verifyPassword(password, teacher.getPassword())) {
                log.error("登录失败：密码错误，邮箱：{}", email);
                throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误");
            }

            // 更新最后登录时间
            log.debug("密码验证通过，更新最后登录时间");
            updateLastLogin(teacher.getTeacherId());

            log.info("登录成功，教师：{}，角色：{}", teacher.getName(), teacher.getRole());
            return teacher;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录过程发生未知错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            if (password == null || hashedPassword == null) {
                log.error("密码验证失败：密码为空");
                return false;
            }

            log.debug("正在验证密码，原始密码长度：{}，哈希密码长度：{}",
                    password.length(), hashedPassword.length());

            boolean matches = passwordEncoder.matches(password, hashedPassword);
            log.debug("密码验证结果：{}", matches ? "匹配" : "不匹配");
            return matches;

        } catch (Exception e) {
            log.error("密码验证过程发生错误", e);
            return false;
        }
    }

    @Override
    public List<Teacher> list() {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Teacher::getTeacherId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Teacher> list(String search) {
        if (search == null || search.trim().isEmpty()) {
            return list();
        }

        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Teacher::getName, search)
                .or()
                .like(Teacher::getEmail, search)
                .or()
                .like(Teacher::getPhone, search)
                .or()
                .like(Teacher::getDepartment, search)
                .orderByAsc(Teacher::getTeacherId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    @CacheEvict(value = "teacherCache", key = "#teacherId")
    public boolean updateRole(Integer teacherId, TeacherRole role) {
        if (teacherId == null || role == null) {
            log.error("更新教师角色失败：参数为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("更新教师角色，教师ID：{}，新角色：{}", teacherId, role);
        return baseMapper.updateRole(teacherId, role) > 0;
    }

    @Override
    @Cacheable(value = "teacherCache", key = "'role:' + #role")
    public List<Teacher> getTeachersByRole(TeacherRole role) {
        if (role == null) {
            log.error("获取教师列表失败：角色为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("获取教师列表，角色：{}", role);
        return baseMapper.selectByRole(role);
    }

    @Override
    public boolean hasRole(Integer teacherId, TeacherRole role) {
        if (teacherId == null || role == null) {
            log.error("检查教师角色失败：参数为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        log.info("检查教师角色，教师ID：{}，角色：{}", teacherId, role);
        return baseMapper.checkRole(teacherId, role) > 0;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return baseMapper.selectList(null);
    }

    @Override
    @Transactional
    public boolean batchDeleteTeachers(List<Integer> teacherIds) {
        try {
            if (teacherIds == null || teacherIds.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "教师ID列表不能为空");
            }

            // 检查每个教师是否可以被删除
            for (Integer teacherId : teacherIds) {
                Teacher teacher = getById(teacherId);
                if (teacher == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND, "教师不存在，ID: " + teacherId);
                }

                // 检查关联数据
                List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignments(teacherId);
                if (!assignments.isEmpty()) {
                    throw new BusinessException(ErrorCode.OPERATION_FAILED,
                            "教师" + teacherId + "还有监考记录，无法删除");
                }

                List<TrainingRecord> trainingRecords = trainingRecordService.getTeacherTrainingRecords(teacherId);
                if (!trainingRecords.isEmpty()) {
                    throw new BusinessException(ErrorCode.OPERATION_FAILED,
                            "教师" + teacherId + "还有培训记录，无法删除");
                }
            }

            // 执行批量删除
            return removeByIds(teacherIds);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除教师失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "批量删除失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Integer> teacherIds, String status) {
        try {
            if (teacherIds == null || teacherIds.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "教师ID列表不能为空");
            }
            if (status == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "状态不能为空");
            }

            TeacherStatus newStatus = TeacherStatus.valueOf(status);
            List<Teacher> teachers = listByIds(teacherIds);

            if (teachers.size() != teacherIds.size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "部分教师不存在");
            }

            teachers.forEach(teacher -> teacher.setStatus(newStatus));
            return updateBatchById(teachers);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的状态值：" + status);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量更新教师状态失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "批量更新状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteTeacher(Integer teacherId) {
        // 检查教师是否存在
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            log.warn("要删除的教师不存在，ID: {}", teacherId);
            return false;
        }

        try {
            // 检查是否有关联的监考记录
            List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignments(teacherId);
            if (!assignments.isEmpty()) {
                log.warn("教师{}还有{}条监考记录，无法删除", teacherId, assignments.size());
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "该教师还有监考记录，请先处理相关记录");
            }

            // 检查是否有关联的培训记录
            List<TrainingRecord> trainingRecords = trainingRecordService.getTeacherTrainingRecords(teacherId);
            if (!trainingRecords.isEmpty()) {
                log.warn("教师{}还有{}条培训记录，无法删除", teacherId, trainingRecords.size());
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "该教师还有培训记录，请先处理相关记录");
            }

            // 如果要删除的是管理员，需要检查是否是最后一个管理员
            if (TeacherRole.ADMIN.equals(teacher.getRole())) {
                long adminCount = count(new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getRole, TeacherRole.ADMIN));
                if (adminCount <= 1) {
                    throw new BusinessException(ErrorCode.OPERATION_FAILED, "系统至少需要保留一个管理员");
                }
            }

            // 软删除：将教师状态设置为已禁用
            teacher.setStatus(TeacherStatus.DISABLED);
            return updateById(teacher);

            // 如果确实需要物理删除，请确保先删除所有关联数据
            // return removeById(teacherId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除教师失败，ID: {}", teacherId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除教师失败：" + e.getMessage());
        }
    }

    @Override
    public PageResult<Teacher> getTeachersByPage(int page, int size) {
        // 确保页码和大小在合理范围内
        page = Math.max(1, page);
        size = Math.max(1, Math.min(size, 100)); // 限制每页最大100条

        // 创建分页请求
        Page<Teacher> pageRequest = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Teacher::getTeacherId);

        // 执行分页查询
        IPage<Teacher> teacherPage = baseMapper.selectPage(pageRequest, wrapper);

        // 返回分页结果
        return new PageResult<>(
                teacherPage.getRecords(),
                teacherPage.getTotal(),
                (int) teacherPage.getPages(),
                (int) teacherPage.getCurrent(),
                (int) teacherPage.getSize());
    }
}