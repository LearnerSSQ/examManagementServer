package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

        /**
         * 更新学习时长
         */
        @Update("UPDATE training_record SET study_time = study_time + #{minutes} WHERE record_id = #{recordId}")
        int updateStudyTime(@Param("recordId") Long recordId, @Param("minutes") Integer minutes);

        /**
         * 更新考试成绩和完成状态
         */
        @Update("UPDATE training_record SET exam_score = #{examScore}, status = 2, complete_time = #{completeTime} WHERE record_id = #{recordId}")
        int completeExam(@Param("recordId") Long recordId, @Param("examScore") Integer examScore,
                        @Param("completeTime") LocalDateTime completeTime);

        /**
         * 更新培训记录状态
         */
        @Update("UPDATE training_record SET status = #{status} WHERE record_id = #{recordId}")
        int updateStatus(@Param("recordId") Long recordId, @Param("status") Integer status);

        @Update("UPDATE training_record SET score = #{score}, status = #{status}, complete_time = #{completeTime} WHERE record_id = #{recordId}")
        int updateScore(@Param("recordId") Long recordId, @Param("score") Integer score,
                        @Param("status") Integer status,
                        @Param("completeTime") LocalDateTime completeTime);

        /**
         * 获取教师培训完成情况
         */
        @Select("SELECT t.teacher_id, t.name, " +
                        "COUNT(DISTINCT tr.material_id) as completed_count, " +
                        "MAX(tr.completion_time) as last_training_time, " +
                        "AVG(tr.quiz_score) as average_quiz_score " +
                        "FROM teacher t " +
                        "LEFT JOIN training_record tr ON t.teacher_id = tr.teacher_id " +
                        "WHERE tr.completion_time BETWEEN #{startDate} AND #{endDate} " +
                        "GROUP BY t.teacher_id, t.name")
        List<Map<String, Object>> getTrainingCompletionStatus(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * 获取未完成培训的教师
         */
        @Select("SELECT t.teacher_id, t.name, t.department " +
                        "FROM teacher t " +
                        "LEFT JOIN training_record tr ON t.teacher_id = tr.teacher_id " +
                        "AND tr.material_id = #{materialId} " +
                        "WHERE tr.record_id IS NULL")
        List<Map<String, Object>> getUncompletedTrainingTeachers(
                        @Param("materialId") Integer materialId);

        /**
         * 获取教师培训详细记录
         */
        @Select("SELECT tr.*, tm.title as material_title, " +
                        "tm.type as material_type " +
                        "FROM training_record tr " +
                        "JOIN training_material tm ON tr.material_id = tm.material_id " +
                        "WHERE tr.teacher_id = #{teacherId} " +
                        "ORDER BY tr.completion_time DESC")
        List<Map<String, Object>> getTeacherTrainingDetails(@Param("teacherId") Integer teacherId);

        /**
         * 获取培训材料的学习统计
         */
        @Select("SELECT tm.material_id, tm.title, " +
                        "COUNT(DISTINCT tr.teacher_id) as completed_count, " +
                        "AVG(tr.quiz_score) as average_score, " +
                        "MIN(tr.completion_time) as first_completion, " +
                        "MAX(tr.completion_time) as last_completion " +
                        "FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "GROUP BY tm.material_id, tm.title")
        List<Map<String, Object>> getMaterialCompletionStats();

        /**
         * 批量插入培训记录
         */
        @Insert("<script>" +
                        "INSERT INTO training_record (teacher_id, material_id, completion_time, " +
                        "quiz_score, study_duration) VALUES " +
                        "<foreach collection='records' item='record' separator=','>" +
                        "(#{record.teacherId}, #{record.materialId}, #{record.completionTime}, " +
                        "#{record.quizScore}, #{record.studyDuration})" +
                        "</foreach>" +
                        "</script>")
        int batchInsertRecords(@Param("records") List<TrainingRecord> records);

        /**
         * 获取部门培训完成情况统计
         */
        @Select("SELECT t.department, " +
                        "COUNT(DISTINCT t.teacher_id) as total_teachers, " +
                        "COUNT(DISTINCT tr.teacher_id) as trained_teachers, " +
                        "AVG(tr.quiz_score) as average_score " +
                        "FROM teacher t " +
                        "LEFT JOIN training_record tr ON t.teacher_id = tr.teacher_id " +
                        "AND tr.material_id = #{materialId} " +
                        "GROUP BY t.department")
        List<Map<String, Object>> getDepartmentTrainingStats(@Param("materialId") Integer materialId);
}