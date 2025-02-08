package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EvaluationService extends IService<Evaluation> {

    /**
     * 添加考评记录
     */
    void addEvaluation(Evaluation evaluation);

    /**
     * 批量添加考评记录
     */
    void batchAddEvaluations(List<Evaluation> evaluations);

    /**
     * 获取教师平均评分
     */
    BigDecimal getTeacherAverageScore(Integer teacherId);

    /**
     * 获取教师历史考评详情
     */
    List<Map<String, Object>> getTeacherEvaluationHistory(Integer teacherId);

    /**
     * 获取教师考评统计信息
     */
    Map<String, Object> getTeacherEvaluationStats(Integer teacherId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取优秀监考教师名单
     */
    List<Map<String, Object>> getExcellentInvigilators(BigDecimal excellentThreshold, Integer minEvaluationCount);

    /**
     * 获取部门教师评价统计
     */
    List<Map<String, Object>> getDepartmentTeacherStats(String departmentId);

    /**
     * 更新评价标准配置
     */
    boolean updateEvaluationCriteria(Map<String, Object> criteria);

    /**
     * 获取教师评分排名
     */
    List<Map<String, Object>> getTeacherScoreRanking(String department, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 计算教师综合评分
     */
    BigDecimal calculateTeacherCompositeScore(Integer teacherId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 生成教师考评报告
     */
    Map<String, Object> generateTeacherEvaluationReport(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * 获取部门评分统计
     */
    Map<String, Object> getDepartmentScoreStatistics(String department, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 检查教师是否达到评分标准
     */
    boolean checkTeacherQualification(Integer teacherId, BigDecimal minimumScore);

    /**
     * 获取监考安排的评价记录
     */
    List<Evaluation> getEvaluationsByAssignment(Long assignmentId);

    /**
     * 获取教师的评价记录
     */
    List<Evaluation> getTeacherEvaluations(Integer teacherId);

    /**
     * 获取监考安排的评价数量
     */
    Integer getEvaluationCount(Long assignmentId);

    /**
     * 检查是否已评价
     */
    boolean hasEvaluated(Long assignmentId, Integer teacherId);
}