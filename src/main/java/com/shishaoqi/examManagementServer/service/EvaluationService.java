package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import java.math.BigDecimal;
import java.util.List;

public interface EvaluationService extends IService<Evaluation> {

    /**
     * 获取监考安排的所有评价
     */
    List<Evaluation> getEvaluationsByAssignment(Long assignmentId);

    /**
     * 获取教师的所有评价
     */
    List<Evaluation> getTeacherEvaluations(Integer teacherId);

    /**
     * 获取教师的平均评分
     */
    BigDecimal getTeacherAverageScore(Integer teacherId);

    /**
     * 获取监考安排的评分数量
     */
    int getEvaluationCount(Long assignmentId);

    /**
     * 检查评价人是否已对该监考安排进行评价
     */
    boolean hasEvaluated(Long assignmentId, Integer evaluatorId);
}