package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

        /**
         * 获取教师的平均评分
         */
        @Select("SELECT AVG(e.score) FROM evaluation e " +
                        "JOIN invigilator_assignment ia ON e.assignment_id = ia.assignment_id " +
                        "WHERE ia.teacher_id = #{teacherId}")
        BigDecimal getTeacherAverageScore(Integer teacherId);

        /**
         * 获取教师历史考评详情
         */
        @Select("SELECT e.*, ia.exam_date, ia.exam_location " +
                        "FROM evaluation e " +
                        "JOIN invigilator_assignment ia ON e.assignment_id = ia.assignment_id " +
                        "WHERE ia.teacher_id = #{teacherId} " +
                        "ORDER BY ia.exam_date DESC")
        List<Map<String, Object>> getTeacherEvaluationHistory(@Param("teacherId") Integer teacherId);

        /**
         * 获取教师考评统计信息
         */
        @Select("SELECT " +
                        "COUNT(*) as total_evaluations, " +
                        "AVG(score) as average_score, " +
                        "SUM(CASE WHEN score >= 90 THEN 1 ELSE 0 END) as excellent_count, " +
                        "SUM(CASE WHEN score >= 80 AND score < 90 THEN 1 ELSE 0 END) as good_count, " +
                        "SUM(CASE WHEN score >= 60 AND score < 80 THEN 1 ELSE 0 END) as pass_count, " +
                        "SUM(CASE WHEN score < 60 THEN 1 ELSE 0 END) as fail_count " +
                        "FROM evaluation e " +
                        "JOIN invigilator_assignment ia ON e.assignment_id = ia.assignment_id " +
                        "WHERE ia.teacher_id = #{teacherId} " +
                        "AND ia.exam_date BETWEEN #{startDate} AND #{endDate}")
        Map<String, Object> getTeacherEvaluationStats(
                        @Param("teacherId") Integer teacherId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * 获取优秀监考教师名单
         */
        @Select("SELECT t.teacher_id, t.name, t.department, " +
                        "COUNT(*) as evaluation_count, " +
                        "AVG(e.score) as average_score " +
                        "FROM teacher t " +
                        "JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "WHERE e.score >= #{excellentThreshold} " +
                        "GROUP BY t.teacher_id, t.name, t.department " +
                        "HAVING evaluation_count >= #{minEvaluationCount} " +
                        "ORDER BY average_score DESC")
        List<Map<String, Object>> getExcellentInvigilators(
                        @Param("excellentThreshold") BigDecimal excellentThreshold,
                        @Param("minEvaluationCount") Integer minEvaluationCount);

        /**
         * 获取部门教师评价统计
         */
        @Select("SELECT t.teacher_id, t.name, t.department, " +
                        "COUNT(e.evaluation_id) as evaluation_count, " +
                        "AVG(e.score) as average_score, " +
                        "MIN(e.score) as lowest_score, " +
                        "MAX(e.score) as highest_score " +
                        "FROM teacher t " +
                        "LEFT JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "WHERE t.department = #{departmentId} " +
                        "GROUP BY t.teacher_id, t.name, t.department")
        List<Map<String, Object>> getDepartmentTeacherStats(@Param("departmentId") String departmentId);

        /**
         * 更新评价标准配置
         */
        @Update("UPDATE evaluation_criteria SET " +
                        "punctuality_weight = #{criteria.punctuality}, " +
                        "responsibility_weight = #{criteria.responsibility}, " +
                        "communication_weight = #{criteria.communication}, " +
                        "professionalism_weight = #{criteria.professionalism}, " +
                        "update_time = NOW()")
        int updateEvaluationCriteria(@Param("criteria") Map<String, Object> criteria);
}