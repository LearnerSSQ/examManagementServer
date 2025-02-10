package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.EvaluationMapper;
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
        // TODO: 实现获取优秀监考教师的逻辑
        return excellentTeachers;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'deptStats:' + #departmentId")
    public List<Map<String, Object>> getDepartmentTeacherStats(String departmentId) {
        if (departmentId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        List<Map<String, Object>> departmentStats = new ArrayList<>();
        // TODO: 实现获取部门教师评价统计的逻辑
        return departmentStats;
    }

    @Override
    @Transactional
    @CacheEvict(value = "evaluationCache", allEntries = true)
    public boolean updateEvaluationCriteria(Map<String, Object> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // TODO: 实现更新评价标准的逻辑
        return true;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'ranking:' + #department + ':' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getTeacherScoreRanking(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        List<Map<String, Object>> rankings = new ArrayList<>();
        // TODO: 实现获取教师评分排名的逻辑
        return rankings;
    }

    @Override
    public BigDecimal calculateTeacherCompositeScore(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (teacherId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // TODO: 实现计算教师综合评分的逻辑
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> generateTeacherEvaluationReport(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (teacherId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> report = new HashMap<>();
        // TODO: 实现生成教师考评报告的逻辑
        return report;
    }

    @Override
    public Map<String, Object> getDepartmentScoreStatistics(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (department == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Map<String, Object> statistics = new HashMap<>();
        // TODO: 实现获取部门评分统计的逻辑
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