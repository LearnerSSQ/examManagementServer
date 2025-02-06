package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;

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
     * 获取监考安排的评分数量
     */
    @Select("SELECT COUNT(*) FROM evaluation WHERE assignment_id = #{assignmentId}")
    int getEvaluationCount(Long assignmentId);
}