package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.repository.EvaluationMapper;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final InvigilatorAssignmentMapper assignmentMapper;

    public EvaluationServiceImpl(InvigilatorAssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public List<Evaluation> getEvaluationsByAssignment(Long assignmentId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId)
                .orderByDesc(Evaluation::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Evaluation> getTeacherEvaluations(Integer teacherId) {
        // 先获取教师的所有监考安排ID
        LambdaQueryWrapper<InvigilatorAssignment> assignmentWrapper = new LambdaQueryWrapper<>();
        assignmentWrapper.eq(InvigilatorAssignment::getTeacherId, teacherId);
        List<Long> assignmentIds = assignmentMapper.selectList(assignmentWrapper)
                .stream()
                .map(InvigilatorAssignment::getAssignmentId)
                .collect(Collectors.toList());

        if (assignmentIds.isEmpty()) {
            return List.of();
        }

        // 获取这些监考安排的评价
        LambdaQueryWrapper<Evaluation> evaluationWrapper = new LambdaQueryWrapper<>();
        evaluationWrapper.in(Evaluation::getAssignmentId, assignmentIds)
                .orderByDesc(Evaluation::getCreateTime);
        return list(evaluationWrapper);
    }

    @Override
    public BigDecimal getTeacherAverageScore(Integer teacherId) {
        return baseMapper.getTeacherAverageScore(teacherId);
    }

    @Override
    public int getEvaluationCount(Long assignmentId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public boolean hasEvaluated(Long assignmentId, Integer evaluatorId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAssignmentId, assignmentId)
                .eq(Evaluation::getEvaluatorId, evaluatorId);
        return count(wrapper) > 0;
    }
}