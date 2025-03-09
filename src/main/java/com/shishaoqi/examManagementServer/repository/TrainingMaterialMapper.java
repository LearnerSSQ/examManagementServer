package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TrainingMaterialMapper extends BaseMapper<TrainingMaterial> {

        /**
         * 获取最新的培训材料
         */
        @Select("SELECT * FROM training_material " +
                        "WHERE status = #{status} " +
                        "AND create_time <= NOW() " +
                        "ORDER BY create_time DESC " +
                        "LIMIT #{limit}")
        List<TrainingMaterial> getLatestMaterials(
                        @Param("status") TrainingMaterialStatus status,
                        @Param("limit") Integer limit);

        /**
         * 获取教师未完成的培训材料
         */
        @Select("SELECT tm.* FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "AND tr.teacher_id = #{teacherId} " +
                        "WHERE tr.record_id IS NULL " +
                        "AND tm.status = #{status} " +
                        "AND tm.create_time <= NOW() " +
                        "ORDER BY tm.create_time DESC")
        List<TrainingMaterial> getUncompletedMaterials(
                        @Param("teacherId") Integer teacherId,
                        @Param("status") TrainingMaterialStatus status);

        /**
         * 获取培训材料的学习情况统计
         */
        @Select("SELECT tm.*, " +
                        "COUNT(DISTINCT tr.teacher_id) as learner_count, " +
                        "AVG(tr.quiz_score) as average_score, " +
                        "SUM(CASE WHEN tr.status = #{recordStatus} THEN 1 ELSE 0 END) as completed_count " +
                        "FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "WHERE tm.create_time BETWEEN #{startDate} AND #{endDate} " +
                        "GROUP BY tm.material_id")
        List<Map<String, Object>> getMaterialStatistics(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("recordStatus") TrainingRecordStatus recordStatus);

        /**
         * 获取必修培训材料列表
         */
        @Select("SELECT * FROM training_material " +
                        "WHERE is_required = 1 " +
                        "AND status = #{status} " +
                        "AND create_time <= NOW() " +
                        "ORDER BY create_time DESC")
        List<TrainingMaterial> getRequiredMaterials(@Param("status") TrainingMaterialStatus status);

        /**
         * 获取培训材料的完成率统计
         */
        @Select("SELECT tm.material_id, tm.title, " +
                        "COUNT(DISTINCT t.teacher_id) as total_teachers, " +
                        "COUNT(DISTINCT tr.teacher_id) as completed_teachers, " +
                        "ROUND(COUNT(DISTINCT tr.teacher_id) * 100.0 / COUNT(DISTINCT t.teacher_id), 2) as completion_rate "
                        +
                        "FROM training_material tm " +
                        "CROSS JOIN teacher t " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "AND tr.teacher_id = t.teacher_id " +
                        "AND tr.status = #{recordStatus} " +
                        "WHERE tm.status = #{materialStatus} " +
                        "GROUP BY tm.material_id, tm.title")
        List<Map<String, Object>> getMaterialCompletionRates(
                        @Param("recordStatus") TrainingRecordStatus recordStatus,
                        @Param("materialStatus") TrainingMaterialStatus materialStatus);

        /**
         * 批量更新培训材料状态
         */
        @Update("<script>" +
                        "<foreach collection='materials' item='material' separator=';'>" +
                        "UPDATE training_material SET " +
                        "status = #{material.status}, " +
                        "update_time = NOW() " +
                        "WHERE material_id = #{material.materialId}" +
                        "</foreach>" +
                        "</script>")
        int batchUpdateStatus(@Param("materials") List<TrainingMaterial> materials);

        /**
         * 获取部门培训材料完成情况
         */
        @Select("SELECT t.department, tm.material_id, tm.title, " +
                        "COUNT(DISTINCT t.teacher_id) as total_teachers, " +
                        "COUNT(DISTINCT CASE WHEN tr.status = #{recordStatus} THEN tr.teacher_id END) as completed_teachers, "
                        +
                        "AVG(tr.quiz_score) as average_score " +
                        "FROM teacher t " +
                        "CROSS JOIN training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "AND tr.teacher_id = t.teacher_id " +
                        "WHERE tm.is_required = 1 " +
                        "GROUP BY t.department, tm.material_id, tm.title")
        List<Map<String, Object>> getDepartmentMaterialStats(
                        @Param("recordStatus") TrainingRecordStatus recordStatus);

        /**
         * 获取培训材料进度
         */
        @Select("SELECT tm.material_id, tm.title, " +
                        "tr.progress, tr.status as record_status, " +
                        "tr.start_time, tr.end_time, tr.quiz_score " +
                        "FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "AND tr.teacher_id = #{teacherId} " +
                        "WHERE tm.material_id = #{materialId}")
        Map<String, Object> selectMaterialProgress(
                        @Param("materialId") Long materialId,
                        @Param("teacherId") Integer teacherId);

        /**
         * 获取培训材料反馈统计
         */
        @Select("SELECT tm.material_id, tm.title, " +
                        "COUNT(DISTINCT tr.teacher_id) as total_feedbacks, " +
                        "AVG(tr.feedback_score) as average_score, " +
                        "COUNT(DISTINCT CASE WHEN tr.feedback_comment IS NOT NULL THEN tr.teacher_id END) as comment_count "
                        +
                        "FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "WHERE tm.material_id = #{materialId} " +
                        "GROUP BY tm.material_id, tm.title")
        Map<String, Object> selectMaterialFeedbackStats(
                        @Param("materialId") Long materialId);

        /**
         * 获取推荐的培训材料
         */
        @Select("SELECT tm.* FROM training_material tm " +
                        "LEFT JOIN training_record tr ON tm.material_id = tr.material_id " +
                        "AND tr.teacher_id = #{teacherId} " +
                        "WHERE tr.record_id IS NULL " +
                        "AND tm.status = 'PUBLISHED' " +
                        "AND tm.create_time <= NOW() " +
                        "ORDER BY tm.is_required DESC, tm.create_time DESC")
        List<TrainingMaterial> selectRecommendedMaterials(
                        @Param("teacherId") Integer teacherId);

        /**
         * 更新培训材料状态
         */
        @Update("UPDATE training_material SET status = #{status} WHERE material_id = #{materialId}")
        int updateStatus(
                        @Param("materialId") Long materialId,
                        @Param("status") TrainingMaterialStatus status);

        /**
         * 更新考试通过分数
         */
        @Update("UPDATE training_material SET pass_score = #{passScore} WHERE material_id = #{materialId}")
        int updatePassScore(
                        @Param("materialId") Long materialId,
                        @Param("passScore") Integer passScore);
}