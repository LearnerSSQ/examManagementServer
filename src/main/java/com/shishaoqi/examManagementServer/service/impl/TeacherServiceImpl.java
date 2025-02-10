package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final EvaluationService evaluationService;
    private final InvigilatorAssignmentService assignmentService;
    private final BCryptPasswordEncoder passwordEncoder;

    public TeacherServiceImpl(
            EvaluationService evaluationService,
            InvigilatorAssignmentService assignmentService,
            BCryptPasswordEncoder passwordEncoder) {
        this.evaluationService = evaluationService;
        this.assignmentService = assignmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void addTeacher(Teacher teacher) {
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
        teacher.setCreateTime(LocalDateTime.now());
        teacher.setStatus(1); // 1: 正常状态
        baseMapper.insert(teacher);
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
    public boolean updateStatus(Integer teacherId, Integer status) {
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

        if (teacher.getStatus() == 2) {
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

        if (teacher.getStatus() == 2) {
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
        wrapper.eq(Teacher::getStatus, 1); // 只选择已激活的教师

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
                    .filter(a -> a.getStatus() == 0 || // 未确认
                            (a.getStatus() == 1 && a.getExamStart().isAfter(LocalDateTime.now()))) // 已确认但未开始
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
                .filter(a -> a.getTeacherId().equals(teacherId) && a.getStatus() != 2)
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
        log.info("获取部门监考工作量统计");
        return baseMapper.getDepartmentWorkloadStats();
    }

    @Override
    public Map<String, Object> getTeacherWorkloadStats(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("获取教师考务工作量统计，教师ID：{}，时间范围：{} 至 {}", teacherId, startDate, endDate);
        Map<String, Object> stats = new HashMap<>();

        // 获取监考任务统计
        List<Map<String, Object>> experienceStats = baseMapper.getTeacherExperienceStats(startDate, endDate);
        Map<String, Object> teacherStats = experienceStats.stream()
                .filter(stat -> teacherId.equals(stat.get("teacher_id")))
                .findFirst()
                .orElse(new HashMap<>());
        stats.put("invigilationStats", teacherStats);

        // 获取培训完成情况
        Map<String, Object> trainingCompletion = getTeacherTrainingCompletion(teacherId);
        stats.put("trainingStats", trainingCompletion);

        // 获取评分情况
        BigDecimal averageScore = evaluationService.getTeacherAverageScore(teacherId);
        stats.put("averageScore", averageScore);

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
        boolean statusValid = teacher != null && teacher.getStatus() == 1;

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
        log.info("获取教师培训完成情况，教师ID：{}", teacherId);
        Map<String, Object> completion = new HashMap<>();

        // 获取教师培训记录
        List<Map<String, Object>> trainingStatus = baseMapper.getTeacherTrainingStatus(null);
        Map<String, Object> teacherStatus = trainingStatus.stream()
                .filter(status -> teacherId.equals(status.get("teacher_id")))
                .findFirst()
                .orElse(new HashMap<>());

        Long completedTrainings = (Long) teacherStatus.get("completed_trainings");
        Long totalTrainings = (Long) teacherStatus.get("total_trainings");

        completion.put("completedCount", completedTrainings);
        completion.put("totalCount", totalTrainings);
        completion.put("completionRate", completedTrainings.doubleValue() / totalTrainings.doubleValue());
        completion.put("isQualified", completedTrainings.equals(totalTrainings));
        completion.put("lastTrainingTime", teacherStatus.get("last_training_time"));

        return completion;
    }

    @Override
    @Transactional
    public boolean batchUpdateQualification(List<Integer> teacherIds, boolean qualified) {
        log.info("批量更新教师监考资格，教师数量：{}，是否具有资格：{}", teacherIds.size(), qualified);
        int status = qualified ? 1 : 0;
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
        wrapper.eq(Teacher::getStatus, 1); // 只选择已激活的教师
        List<Teacher> teachers = list(wrapper);

        return teachers.stream()
                .filter(teacher -> {
                    BigDecimal avgScore = evaluationService.getTeacherAverageScore(teacher.getTeacherId());
                    return avgScore != null && avgScore.doubleValue() >= minScore;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> getInvigilationStatistics(Integer teacherId) {
        log.info("获取教师监考统计信息，教师ID：{}", teacherId);
        Map<String, Object> stats = new HashMap<>();

        // 获取监考任务统计
        List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignments(teacherId);
        long totalAssignments = assignments.size();
        long completedAssignments = assignments.stream()
                .filter(a -> a.getStatus() == 2)
                .count();

        stats.put("totalAssignments", totalAssignments);
        stats.put("completedAssignments", completedAssignments);
        stats.put("averageScore", evaluationService.getTeacherAverageScore(teacherId));

        return stats;
    }

    @Override
    public Map<String, Integer> getWorkloadStatistics(Integer teacherId, int year) {
        log.info("获取教师考务工作量统计，教师ID：{}，年份：{}", teacherId, year);
        Map<String, Integer> workload = new HashMap<>();

        LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59);

        List<InvigilatorAssignment> assignments = assignmentService.getAssignmentsByTimeRange(startTime, endTime)
                .stream()
                .filter(a -> a.getTeacherId().equals(teacherId))
                .collect(Collectors.toList());

        IntStream.rangeClosed(1, 12).forEach(month -> {
            final int currentMonth = month;
            int monthlyCount = (int) assignments.stream()
                    .filter(a -> a.getExamStart().getMonthValue() == currentMonth)
                    .count();
            workload.put(String.valueOf(currentMonth), monthlyCount);
        });

        return workload;
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
        log.info("获取教师监考历史评价，教师ID：{}", teacherId);
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusYears(1);

        List<InvigilatorAssignment> assignments = assignmentService.getAssignmentsByTimeRange(startTime, endTime);
        List<Map<String, Object>> history = new ArrayList<>();

        assignments.stream()
                .filter(a -> a.getTeacherId().equals(teacherId))
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
        wrapper.eq(Teacher::getStatus, 1);
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
        if (email == null || password == null) {
            log.error("登录失败：邮箱或密码为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = getTeacherByEmail(email);
        if (teacher == null) {
            log.error("登录失败：教师不存在，邮箱：{}", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (teacher.getStatus() == 2) {
            log.error("登录失败：教师账号已禁用，邮箱：{}", email);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        if (!verifyPassword(password, teacher.getPassword())) {
            log.error("登录失败：密码错误，邮箱：{}", email);
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 更新最后登录时间
        updateLastLogin(teacher.getTeacherId());

        return teacher;
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}