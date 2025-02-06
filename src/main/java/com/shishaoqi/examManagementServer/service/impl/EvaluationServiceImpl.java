package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.EvaluationMapper;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);
    private static final BigDecimal MAX_SCORE = new BigDecimal("100");

    private final InvigilatorAssignmentMapper assignmentMapper;

    public EvaluationServiceImpl(InvigilatorAssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public List<Evaluation> getEvaluationsByAssignment(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取评价列表失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取评价列表失败：监考安排不存在，ID={}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId)
                .orderByDesc(Evaluation::getCreateTime);
        List<Evaluation> evaluations = list(wrapper);
        log.info("获取监考安排[{}]的评价列表，共{}条", assignmentId, evaluations.size());
        return evaluations;
    }

    @Override
    public List<Evaluation> getTeacherEvaluations(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取教师评价失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 通过监考安排关联查询
        List<Long> assignmentIds = assignmentMapper.selectAssignmentIdsByTeacherId(teacherId);
        if (assignmentIds.isEmpty()) {
            log.info("教师[{}]没有监考安排记录", teacherId);
            return List.of();
        }

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Evaluation::getAssignmentId, assignmentIds)
                .orderByDesc(Evaluation::getCreateTime);
        List<Evaluation> evaluations = list(wrapper);
        log.info("获取教师[{}]的评价列表，共{}条", teacherId, evaluations.size());
        return evaluations;
    }

    @Override
    public BigDecimal getTeacherAverageScore(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取平均评分失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 通过监考安排关联查询
        List<Long> assignmentIds = assignmentMapper.selectAssignmentIdsByTeacherId(teacherId);
        if (assignmentIds.isEmpty()) {
            log.info("教师[{}]暂无评价记录", teacherId);
            return BigDecimal.ZERO;
        }

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Evaluation::getAssignmentId, assignmentIds);
        List<Evaluation> evaluations = list(wrapper);

        if (evaluations.isEmpty()) {
            log.info("教师[{}]暂无评价记录", teacherId);
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = evaluations.stream()
                .map(Evaluation::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal result = totalScore.divide(BigDecimal.valueOf(evaluations.size()), 1, RoundingMode.HALF_UP);
        log.info("教师[{}]的平均评分：{}", teacherId, result);
        return result;
    }

    @Override
    public int getEvaluationCount(Long assignmentId) {
        if (assignmentId == null) {
            log.error("获取评价数量失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            log.error("获取评价数量失败：监考安排不存在，ID={}", assignmentId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId);
        long count = count(wrapper);
        log.info("监考安排[{}]的评价数量：{}", assignmentId, count);
        return Math.toIntExact(count);
    }

    @Override
    public boolean hasEvaluated(Long assignmentId, Integer evaluatorId) {
        if (assignmentId == null || evaluatorId == null) {
            log.error("检查评价状态失败：参数为空，assignmentId={}, evaluatorId={}", assignmentId, evaluatorId);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId)
                .eq(Evaluation::getEvaluatorId, evaluatorId);
        boolean exists = count(wrapper) > 0;
        log.info("检查评价人[{}]是否已评价监考安排[{}]：{}", evaluatorId, assignmentId, exists ? "已评价" : "未评价");
        return exists;
    }

    @Override
    public boolean save(Evaluation evaluation) {
        validateEvaluation(evaluation);
        validateScore(evaluation.getScore());

        // 验证监考安排是否存在
        InvigilatorAssignment assignment = assignmentMapper.selectById(evaluation.getAssignmentId());
        if (assignment == null) {
            log.error("保存评价失败：监考安排不存在，ID={}", evaluation.getAssignmentId());
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 检查是否已评价
        if (hasEvaluated(evaluation.getAssignmentId(), evaluation.getEvaluatorId())) {
            log.error("保存评价失败：该评价人已对此监考安排进行评价");
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_EXISTS);
        }

        boolean success = super.save(evaluation);
        if (success) {
            log.info("成功保存评价，ID：{}，评分：{}", evaluation.getEvaluationId(), evaluation.getScore());
        } else {
            log.error("保存评价失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    public boolean updateById(Evaluation evaluation) {
        if (evaluation == null || evaluation.getEvaluationId() == null) {
            log.error("更新评价失败：评价ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证评价是否存在
        Evaluation existingEvaluation = getById(evaluation.getEvaluationId());
        if (existingEvaluation == null) {
            log.error("更新评价失败：评价不存在，ID={}", evaluation.getEvaluationId());
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (evaluation.getScore() != null) {
            validateScore(evaluation.getScore());
        }

        boolean success = super.updateById(evaluation);
        if (success) {
            log.info("成功更新评价，ID：{}", evaluation.getEvaluationId());
        } else {
            log.error("更新评价失败，ID：{}", evaluation.getEvaluationId());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            log.error("删除评价失败：评价ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        boolean success = super.removeById(id);
        if (success) {
            log.info("成功删除评价，ID：{}", id);
        } else {
            log.error("删除评价失败，ID：{}", id);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    private void validateEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            log.error("评价验证失败：评价对象为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (evaluation.getAssignmentId() == null) {
            log.error("评价验证失败：监考安排ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (evaluation.getEvaluatorId() == null) {
            log.error("评价验证失败：评价人ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (evaluation.getScore() == null) {
            log.error("评价验证失败：评分为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private void validateScore(BigDecimal score) {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(MAX_SCORE) > 0) {
            log.error("评分验证失败：评分无效 {}", score);
            throw new BusinessException(ErrorCode.INVALID_SCORE);
        }
    }
}