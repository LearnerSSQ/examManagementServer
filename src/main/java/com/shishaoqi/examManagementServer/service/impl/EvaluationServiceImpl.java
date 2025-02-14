package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.invigilation.Evaluation;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.EvaluationMapper;
import com.shishaoqi.examManagementServer.repository.TeacherMapper;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    @Transactional
    public void addEvaluation(Evaluation evaluation) {
        validateEvaluation(evaluation);
        save(evaluation);
        log.info("成功添加考评记录，评价ID：{}", evaluation.getEvaluationId());
    }

    @Override
    @Transactional
    public void batchAddEvaluations(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        evaluations.forEach(this::validateEvaluation);
        saveBatch(evaluations);
        log.info("成功批量添加考评记录，数量：{}", evaluations.size());
    }

    private BigDecimal calculateAverage(BigDecimal total, int count) {
        return count > 0 ? total.divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'avgScore:' + #teacherId")
    public BigDecimal getTeacherAverageScore(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Evaluation> evaluations = lambdaQuery()
                .in(Evaluation::getAssignmentId, assignmentIds)
                .list();

        if (evaluations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = evaluations.stream()
                .map(Evaluation::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return calculateAverage(totalScore, evaluations.size());
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'history:' + #teacherId")
    public List<Map<String, Object>> getTeacherEvaluationHistory(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 获取教师的所有监考安排
        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取评价记录并转换为历史记录格式
        List<Evaluation> evaluations = lambdaQuery()
                .in(Evaluation::getAssignmentId, assignmentIds)
                .orderByDesc(Evaluation::getCreateTime)
                .list();

        return evaluations.stream().map(eval -> {
            Map<String, Object> history = new HashMap<>();
            history.put("evaluationId", eval.getEvaluationId());
            history.put("assignmentId", eval.getAssignmentId());
            history.put("evaluatorId", eval.getEvaluatorId());
            history.put("score", eval.getScore());
            history.put("comment", eval.getComment());
            history.put("createTime", eval.getCreateTime());
            return history;
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'stats:' + #teacherId + ':' + #startDate + ':' + #endDate")
    public Map<String, Object> getTeacherEvaluationStats(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime) {
        if (teacherId == null || startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .ge(InvigilatorAssignment::getExamStart, startTime)
                .le(InvigilatorAssignment::getExamEnd, endTime)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        if (assignmentIds.isEmpty()) {
            stats.put("totalEvaluations", 0);
            stats.put("averageScore", 0);
            stats.put("highestScore", 0);
            stats.put("lowestScore", 0);
            return stats;
        }

        List<Evaluation> evaluations = lambdaQuery()
                .in(Evaluation::getAssignmentId, assignmentIds)
                .list();

        int totalEvaluations = evaluations.size();
        BigDecimal totalScore = evaluations.stream()
                .map(Evaluation::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageScore = calculateAverage(totalScore, totalEvaluations);

        Optional<BigDecimal> highestScore = evaluations.stream()
                .map(Evaluation::getScore)
                .max(BigDecimal::compareTo);

        Optional<BigDecimal> lowestScore = evaluations.stream()
                .map(Evaluation::getScore)
                .min(BigDecimal::compareTo);

        stats.put("totalEvaluations", totalEvaluations);
        stats.put("averageScore", averageScore);
        stats.put("highestScore", highestScore.orElse(BigDecimal.ZERO));
        stats.put("lowestScore", lowestScore.orElse(BigDecimal.ZERO));
        stats.put("startTime", startTime);
        stats.put("endTime", endTime);

        return stats;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'excellent:' + #excellentThreshold + ':' + #minEvaluationCount")
    public List<Map<String, Object>> getExcellentInvigilators(BigDecimal excellentThreshold,
            Integer minEvaluationCount) {
        if (excellentThreshold == null || minEvaluationCount == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 获取所有教师的评价统计
        List<Map<String, Object>> excellentTeachers = new ArrayList<>();

        // 获取所有监考安排
        List<InvigilatorAssignment> assignments = assignmentService.list();

        // 按教师ID分组统计
        Map<Integer, List<Evaluation>> teacherEvaluations = assignments.stream()
                .map(assignment -> {
                    List<Evaluation> evals = this.getByAssignmentId(assignment.getAssignmentId());
                    return new AbstractMap.SimpleEntry<>(assignment.getTeacherId(), evals);
                })
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.flatMapping(entry -> entry.getValue().stream(), Collectors.toList())));

        // 筛选优秀教师
        teacherEvaluations.forEach((teacherId, evaluations) -> {
            // 只处理评价数量达到最小要求的教师
            if (evaluations.size() >= minEvaluationCount) {
                // 计算平均分
                BigDecimal averageScore = evaluations.stream()
                        .map(Evaluation::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);

                // 如果平均分达到优秀标准
                if (averageScore.compareTo(excellentThreshold) >= 0) {
                    Teacher teacher = teacherMapper.selectById(teacherId);
                    if (teacher != null) {
                        Map<String, Object> teacherInfo = new HashMap<>();
                        teacherInfo.put("teacherId", teacherId);
                        teacherInfo.put("name", teacher.getName());
                        teacherInfo.put("department", teacher.getDepartment());
                        teacherInfo.put("averageScore", averageScore);
                        teacherInfo.put("evaluationCount", evaluations.size());
                        excellentTeachers.add(teacherInfo);
                    }
                }
            }
        });

        // 按平均分降序排序
        excellentTeachers.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("averageScore");
            BigDecimal scoreB = (BigDecimal) b.get("averageScore");
            return scoreB.compareTo(scoreA);
        });

        return excellentTeachers;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'deptStats:' + #departmentId")
    public List<Map<String, Object>> getDepartmentTeacherStats(String departmentId) {
        if (departmentId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        List<Map<String, Object>> departmentStats = new ArrayList<>();

        // 获取部门所有教师
        List<Teacher> teachers = teacherMapper.selectByDepartment(departmentId);

        // 统计每个教师的评价情况
        for (Teacher teacher : teachers) {
            Map<String, Object> teacherStats = new HashMap<>();
            teacherStats.put("teacherId", teacher.getTeacherId());
            teacherStats.put("name", teacher.getName());

            // 获取教师的所有监考安排
            List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignments(teacher.getTeacherId());

            // 获取所有评价
            List<Evaluation> evaluations = assignments.stream()
                    .flatMap(assignment -> getByAssignmentId(assignment.getAssignmentId()).stream())
                    .collect(Collectors.toList());

            // 计算统计数据
            BigDecimal averageScore = evaluations.stream()
                    .map(Evaluation::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(1, evaluations.size())), 2, RoundingMode.HALF_UP);

            teacherStats.put("evaluationCount", evaluations.size());
            teacherStats.put("averageScore", averageScore);

            // 计算优秀评价比例（90分以上）
            long excellentCount = evaluations.stream()
                    .filter(e -> e.getScore().compareTo(new BigDecimal("90")) >= 0)
                    .count();
            double excellentRate = evaluations.isEmpty() ? 0 : (double) excellentCount / evaluations.size();
            teacherStats.put("excellentRate", excellentRate);

            departmentStats.add(teacherStats);
        }

        return departmentStats;
    }

    @Override
    @Transactional
    @CacheEvict(value = "evaluationCache", allEntries = true)
    public boolean updateEvaluationCriteria(Map<String, Object> criteria) {
        log.info("开始更新评价标准");
        try {
            // 验证参数
            if (criteria == null || criteria.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "评价标准参数不能为空");
            }

            // 获取并验证各项标准
            BigDecimal excellentThreshold = getBigDecimalFromMap(criteria, "excellentThreshold");
            BigDecimal qualifiedThreshold = getBigDecimalFromMap(criteria, "qualifiedThreshold");
            Integer minEvaluationCount = getIntegerFromMap(criteria, "minEvaluationCount");

            // 验证阈值的合理性
            validateThresholds(excellentThreshold, qualifiedThreshold, minEvaluationCount);

            // 更新到数据库
            Map<String, Object> params = new HashMap<>();
            params.put("excellentThreshold", excellentThreshold);
            params.put("qualifiedThreshold", qualifiedThreshold);
            params.put("minEvaluationCount", minEvaluationCount);

            int result = baseMapper.updateEvaluationCriteria(params);
            boolean updated = result > 0;

            if (updated) {
                log.info("评价标准更新成功");
            } else {
                log.warn("评价标准更新失败");
            }

            return updated;
        } catch (BusinessException e) {
            log.error("更新评价标准时发生业务异常：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新评价标准时发生系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新评价标准失败");
        }
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'ranking:' + #department + ':' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getTeacherScoreRanking(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("开始获取教师评分排名，部门：{}，时间范围：{} - {}", department, startDate, endDate);
        try {
            // 获取部门内的所有教师
            List<Teacher> teachers = teacherMapper.selectByDepartment(department);
            if (teachers.isEmpty()) {
                log.warn("未找到部门{}的教师", department);
                return new ArrayList<>();
            }

            // 获取每个教师的评分统计
            List<Map<String, Object>> rankings = new ArrayList<>();
            for (Teacher teacher : teachers) {
                // 获取教师在指定时间范围内的评价统计
                Map<String, Object> stats = getTeacherEvaluationStats(teacher.getTeacherId(), startDate, endDate);

                Map<String, Object> rankInfo = new HashMap<>();
                rankInfo.put("teacherId", teacher.getTeacherId());
                rankInfo.put("teacherName", teacher.getName());
                rankInfo.put("averageScore", stats.get("averageScore"));
                rankInfo.put("evaluationCount", stats.get("totalEvaluations"));
                rankInfo.put("excellentRate", stats.get("excellentRate"));

                rankings.add(rankInfo);
            }

            // 按平均分降序排序
            rankings.sort((a, b) -> {
                BigDecimal scoreA = (BigDecimal) a.get("averageScore");
                BigDecimal scoreB = (BigDecimal) b.get("averageScore");
                return scoreB.compareTo(scoreA);
            });

            // 添加排名信息
            for (int i = 0; i < rankings.size(); i++) {
                rankings.get(i).put("rank", i + 1);
            }

            log.info("成功获取{}个教师的评分排名", rankings.size());
            return rankings;
        } catch (BusinessException e) {
            log.error("获取教师评分排名时发生业务异常：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取教师评分排名时发生系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取教师评分排名失败");
        }
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "不能为空");
        }
        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof String) {
                return new BigDecimal((String) value);
            } else if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "格式不正确");
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "格式不正确");
        }
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "不能为空");
        }
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "格式不正确");
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, key + "格式不正确");
        }
    }

    private void validateThresholds(BigDecimal excellentThreshold, BigDecimal qualifiedThreshold,
            Integer minEvaluationCount) {
        if (excellentThreshold.compareTo(BigDecimal.ZERO) < 0
                || excellentThreshold.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优秀阈值必须在0-100之间");
        }
        if (qualifiedThreshold.compareTo(BigDecimal.ZERO) < 0
                || qualifiedThreshold.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "合格阈值必须在0-100之间");
        }
        if (excellentThreshold.compareTo(qualifiedThreshold) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优秀阈值必须大于合格阈值");
        }
        if (minEvaluationCount < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最小评价数量必须大于0");
        }
    }

    @Override
    public BigDecimal calculateTeacherCompositeScore(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (teacherId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 获取时间范围内的监考安排
        List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignmentsByTimeRange(
                teacherId, startDate, endDate);

        if (assignments.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 获取所有评价
        List<Evaluation> evaluations = assignments.stream()
                .flatMap(assignment -> getByAssignmentId(assignment.getAssignmentId()).stream())
                .collect(Collectors.toList());

        if (evaluations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算评分占比（70%）
        BigDecimal averageScore = evaluations.stream()
                .map(Evaluation::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(0.7));

        // 计算任务完成率占比（30%）
        long completedCount = assignments.stream()
                .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED) // 已完成的监考
                .count();
        BigDecimal completionRate = BigDecimal.valueOf(completedCount)
                .divide(BigDecimal.valueOf(assignments.size()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(30)); // 转换为百分制

        // 返回综合评分
        return averageScore.add(completionRate);
    }

    @Override
    public Map<String, Object> generateTeacherEvaluationReport(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (teacherId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> report = new HashMap<>();

        // 获取教师基本信息
        Teacher teacher = teacherMapper.selectById(teacherId);
        report.put("teacherInfo", teacher);

        // 获取时间范围内的监考安排
        List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignmentsByTimeRange(
                teacherId, startDate, endDate);

        // 统计监考任务情况
        Map<String, Object> assignmentStats = new HashMap<>();
        assignmentStats.put("totalCount", assignments.size());
        assignmentStats.put("completedCount",
                assignments.stream().filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED).count());
        assignmentStats.put("canceledCount",
                assignments.stream().filter(a -> a.getStatus() == InvigilatorAssignmentStatus.CANCELLED).count());
        report.put("assignmentStats", assignmentStats);

        // 获取评价统计
        List<Evaluation> evaluations = assignments.stream()
                .flatMap(assignment -> getByAssignmentId(assignment.getAssignmentId()).stream())
                .collect(Collectors.toList());

        Map<String, Object> evaluationStats = new HashMap<>();
        if (!evaluations.isEmpty()) {
            // 计算平均分
            BigDecimal averageScore = evaluations.stream()
                    .map(Evaluation::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(evaluations.size()), 2, RoundingMode.HALF_UP);

            // 计算优秀评价比例
            long excellentCount = evaluations.stream()
                    .filter(e -> e.getScore().compareTo(new BigDecimal("90")) >= 0)
                    .count();
            double excellentRate = (double) excellentCount / evaluations.size();

            evaluationStats.put("evaluationCount", evaluations.size());
            evaluationStats.put("averageScore", averageScore);
            evaluationStats.put("excellentCount", excellentCount);
            evaluationStats.put("excellentRate", excellentRate);
        }
        report.put("evaluationStats", evaluationStats);

        // 计算综合评分
        BigDecimal compositeScore = calculateTeacherCompositeScore(teacherId, startDate, endDate);
        report.put("compositeScore", compositeScore);

        return report;
    }

    @Override
    public Map<String, Object> getDepartmentScoreStatistics(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (department == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> statistics = new HashMap<>();

        // 获取部门所有教师
        List<Teacher> teachers = teacherMapper.selectByDepartment(department);

        // 获取所有教师的评价数据
        List<BigDecimal> allScores = new ArrayList<>();
        Map<Integer, BigDecimal> teacherScores = new HashMap<>();

        for (Teacher teacher : teachers) {
            // 计算每个教师的综合评分
            BigDecimal score = calculateTeacherCompositeScore(teacher.getTeacherId(), startDate, endDate);
            if (score.compareTo(BigDecimal.ZERO) > 0) {
                allScores.add(score);
                teacherScores.put(teacher.getTeacherId(), score);
            }
        }

        if (!allScores.isEmpty()) {
            // 计算部门平均分
            BigDecimal departmentAverage = allScores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allScores.size()), 2, RoundingMode.HALF_UP);

            // 计算最高分和最低分
            BigDecimal highestScore = allScores.stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal lowestScore = allScores.stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // 计算优秀教师比例（90分以上）
            long excellentCount = allScores.stream()
                    .filter(score -> score.compareTo(new BigDecimal("90")) >= 0)
                    .count();
            double excellentRate = (double) excellentCount / allScores.size();

            statistics.put("departmentAverage", departmentAverage);
            statistics.put("highestScore", highestScore);
            statistics.put("lowestScore", lowestScore);
            statistics.put("totalTeachers", teachers.size());
            statistics.put("evaluatedTeachers", allScores.size());
            statistics.put("excellentCount", excellentCount);
            statistics.put("excellentRate", excellentRate);

            // 获取前三名教师
            List<Map<String, Object>> topTeachers = teacherScores.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(3)
                    .map(entry -> {
                        Map<String, Object> teacherInfo = new HashMap<>();
                        Teacher teacher = teachers.stream()
                                .filter(t -> t.getTeacherId().equals(entry.getKey()))
                                .findFirst()
                                .orElse(null);
                        if (teacher != null) {
                            teacherInfo.put("teacherId", teacher.getTeacherId());
                            teacherInfo.put("name", teacher.getName());
                            teacherInfo.put("score", entry.getValue());
                        }
                        return teacherInfo;
                    })
                    .collect(Collectors.toList());

            statistics.put("topTeachers", topTeachers);
        }

        return statistics;
    }

    @Override
    public boolean checkTeacherQualification(Integer teacherId, BigDecimal minimumScore) {
        if (teacherId == null || minimumScore == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        BigDecimal averageScore = getTeacherAverageScore(teacherId);
        return averageScore.compareTo(minimumScore) >= 0;
    }

    @Override
    public List<Evaluation> getEvaluationsByAssignment(Long assignmentId) {
        if (assignmentId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
    }

    @Override
    public List<Evaluation> getTeacherEvaluations(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 获取教师的所有监考安排
        List<Long> assignmentIds = assignmentService.lambdaQuery()
                .eq(InvigilatorAssignment::getTeacherId, teacherId)
                .list()
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return new ArrayList<>();
        }

        return lambdaQuery()
                .in(Evaluation::getAssignmentId, assignmentIds)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
    }

    @Override
    public Integer getEvaluationCount(Long assignmentId) {
        if (assignmentId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        return Math.toIntExact(lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .count());
    }

    @Override
    public boolean hasEvaluated(Long assignmentId, Integer teacherId) {
        if (assignmentId == null || teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .eq(Evaluation::getEvaluatorId, teacherId)
                .count() > 0;
    }

    @Override
    public List<Evaluation> getByAssignmentId(Long assignmentId) {
        if (assignmentId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    public boolean createEvaluation(Long assignmentId, Integer evaluatorId, Double score, String comment) {
        if (assignmentId == null || evaluatorId == null || score == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 检查监考安排是否存在
        InvigilatorAssignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 检查是否已评价
        if (hasEvaluated(assignmentId, evaluatorId)) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_EXISTS);
        }

        // 创建评价记录
        Evaluation evaluation = new Evaluation();
        evaluation.setAssignmentId(assignmentId);
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setScore(BigDecimal.valueOf(score));
        evaluation.setComment(comment);
        evaluation.setCreateTime(LocalDateTime.now());

        return save(evaluation);
    }

    @Override
    @Transactional
    public boolean batchCreateEvaluations(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        evaluations.forEach(this::validateEvaluation);
        return saveBatch(evaluations);
    }

    @Override
    public Map<String, Object> getEvaluationStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> statistics = new HashMap<>();

        long totalEvaluations = lambdaQuery()
                .between(Evaluation::getCreateTime, startTime, endTime)
                .count();

        List<Evaluation> evaluations = lambdaQuery()
                .between(Evaluation::getCreateTime, startTime, endTime)
                .list();

        BigDecimal totalScore = evaluations.stream()
                .map(Evaluation::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageScore = calculateAverage(totalScore, evaluations.size());

        statistics.put("totalEvaluations", totalEvaluations);
        statistics.put("averageScore", averageScore);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);

        return statistics;
    }

    private void validateEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (evaluation.getAssignmentId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "监考安排ID不能为空");
        }
        if (evaluation.getEvaluatorId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评价人ID不能为空");
        }
        if (evaluation.getScore() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分不能为空");
        }
        if (evaluation.getScore().compareTo(BigDecimal.ZERO) < 0
                || evaluation.getScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "评分必须在0-100之间");
        }
    }
}