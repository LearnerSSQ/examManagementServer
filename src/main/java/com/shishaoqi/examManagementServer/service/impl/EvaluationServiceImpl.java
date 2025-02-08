package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.repository.EvaluationMapper;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Override
    @Transactional
    public void addEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new BusinessException("考评记录不能为空");
        }
        evaluation.setCreateTime(LocalDateTime.now());
        evaluationMapper.insert(evaluation);
    }

    @Override
    @Transactional
    public void batchAddEvaluations(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            throw new BusinessException("考评记录列表不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        evaluations.forEach(eval -> eval.setCreateTime(now));
        evaluations.forEach(evaluationMapper::insert);
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'avgScore:' + #teacherId")
    public BigDecimal getTeacherAverageScore(Integer teacherId) {
        return evaluationMapper.getTeacherAverageScore(teacherId);
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'history:' + #teacherId")
    public List<Map<String, Object>> getTeacherEvaluationHistory(Integer teacherId) {
        return evaluationMapper.getTeacherEvaluationHistory(teacherId);
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'stats:' + #teacherId + ':' + #startDate + ':' + #endDate")
    public Map<String, Object> getTeacherEvaluationStats(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return evaluationMapper.getTeacherEvaluationStats(teacherId, startDate, endDate);
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'excellent:' + #excellentThreshold + ':' + #minEvaluationCount")
    public List<Map<String, Object>> getExcellentInvigilators(BigDecimal excellentThreshold,
            Integer minEvaluationCount) {
        return evaluationMapper.getExcellentInvigilators(excellentThreshold, minEvaluationCount);
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'deptStats:' + #departmentId")
    public List<Map<String, Object>> getDepartmentTeacherStats(String departmentId) {
        return evaluationMapper.getDepartmentTeacherStats(departmentId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "evaluationCache", allEntries = true)
    public boolean updateEvaluationCriteria(Map<String, Object> criteria) {
        return evaluationMapper.updateEvaluationCriteria(criteria) > 0;
    }

    @Override
    @Cacheable(value = "evaluationCache", key = "'ranking:' + #department + ':' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getTeacherScoreRanking(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Map<String, Object>> stats = evaluationMapper.getDepartmentTeacherStats(department);
        stats.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("average_score");
            BigDecimal scoreB = (BigDecimal) b.get("average_score");
            return scoreB.compareTo(scoreA);
        });
        return stats;
    }

    @Override
    public BigDecimal calculateTeacherCompositeScore(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        Map<String, Object> stats = getTeacherEvaluationStats(teacherId, startDate, endDate);
        BigDecimal averageScore = (BigDecimal) stats.get("average_score");
        Long excellentCount = (Long) stats.get("excellent_count");
        Long totalEvaluations = (Long) stats.get("total_evaluations");

        if (totalEvaluations == 0) {
            return BigDecimal.ZERO;
        }

        // 计算综合评分：平均分*0.7 + 优秀率*0.3
        BigDecimal excellentRate = new BigDecimal(excellentCount)
                .divide(new BigDecimal(totalEvaluations), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        return averageScore.multiply(new BigDecimal("0.7"))
                .add(excellentRate.multiply(new BigDecimal("0.3")));
    }

    @Override
    public Map<String, Object> generateTeacherEvaluationReport(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();

        // 基本统计信息
        Map<String, Object> stats = getTeacherEvaluationStats(teacherId, startDate, endDate);
        report.put("statistics", stats);

        // 历史考评记录
        List<Map<String, Object>> history = getTeacherEvaluationHistory(teacherId);
        report.put("history", history);

        // 综合评分
        BigDecimal compositeScore = calculateTeacherCompositeScore(teacherId, startDate, endDate);
        report.put("compositeScore", compositeScore);

        // 评分趋势分析
        List<Map<String, Object>> trend = new ArrayList<>();
        history.forEach(record -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", record.get("exam_date"));
            point.put("score", record.get("score"));
            trend.add(point);
        });
        report.put("scoreTrend", trend);

        return report;
    }

    @Override
    public Map<String, Object> getDepartmentScoreStatistics(String department, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Map<String, Object>> teacherStats = getDepartmentTeacherStats(department);

        Map<String, Object> statistics = new HashMap<>();
        DoubleSummaryStatistics stats = teacherStats.stream()
                .map(stat -> ((BigDecimal) stat.get("average_score")).doubleValue())
                .collect(DoubleSummaryStatistics::new,
                        DoubleSummaryStatistics::accept,
                        DoubleSummaryStatistics::combine);

        statistics.put("averageScore", stats.getAverage());
        statistics.put("maxScore", stats.getMax());
        statistics.put("minScore", stats.getMin());
        statistics.put("teacherCount", stats.getCount());

        return statistics;
    }

    @Override
    public boolean checkTeacherQualification(Integer teacherId, BigDecimal minimumScore) {
        BigDecimal averageScore = getTeacherAverageScore(teacherId);
        return averageScore != null && averageScore.compareTo(minimumScore) >= 0;
    }

    @Override
    public List<Evaluation> getEvaluationsByAssignment(Long assignmentId) {
        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
    }

    @Override
    public List<Evaluation> getTeacherEvaluations(Integer teacherId) {
        return lambdaQuery()
                .eq(Evaluation::getEvaluatorId, teacherId)
                .orderByDesc(Evaluation::getCreateTime)
                .list();
    }

    @Override
    public Integer getEvaluationCount(Long assignmentId) {
        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .count().intValue();
    }

    @Override
    public boolean hasEvaluated(Long assignmentId, Integer teacherId) {
        return lambdaQuery()
                .eq(Evaluation::getAssignmentId, assignmentId)
                .eq(Evaluation::getEvaluatorId, teacherId)
                .count() > 0;
    }
}