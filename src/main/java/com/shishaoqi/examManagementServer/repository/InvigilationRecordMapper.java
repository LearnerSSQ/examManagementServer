package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecord;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface InvigilationRecordMapper extends BaseMapper<InvigilationRecord> {

        /**
         * 获取监考安排的所有记录
         */
        @Select("SELECT * FROM invigilation_record WHERE assignment_id = #{assignmentId} ORDER BY create_time DESC")
        List<InvigilationRecord> getRecordsByAssignment(Long assignmentId);

        /**
         * 获取监考安排的签到记录
         */
        @Select("SELECT * FROM invigilation_record WHERE assignment_id = #{assignmentId} AND type = 1")
        InvigilationRecord getSignInRecord(Long assignmentId);

        /**
         * 获取监考过程记录
         */
        @Select("SELECT ir.*, ia.exam_date, ia.exam_location, " +
                        "t.name as teacher_name " +
                        "FROM invigilation_record ir " +
                        "JOIN invigilator_assignment ia ON ir.assignment_id = ia.assignment_id " +
                        "JOIN teacher t ON ia.teacher_id = t.teacher_id " +
                        "WHERE ir.exam_date = #{examDate} " +
                        "ORDER BY ir.record_time DESC")
        List<Map<String, Object>> getInvigilationRecords(@Param("examDate") LocalDateTime examDate);

        /**
         * 获取异常记录统计
         */
        @Select("SELECT ir.incident_type, " +
                        "COUNT(*) as incident_count " +
                        "FROM invigilation_record ir " +
                        "WHERE ir.exam_date BETWEEN #{startDate} AND #{endDate} " +
                        "AND ir.incident_type IS NOT NULL " +
                        "GROUP BY ir.incident_type")
        List<Map<String, Object>> getIncidentStatistics(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * 获取教师监考记录详情
         */
        @Select("SELECT ir.*, ia.exam_date, ia.exam_location, " +
                        "e.score as evaluation_score " +
                        "FROM invigilation_record ir " +
                        "JOIN invigilator_assignment ia ON ir.assignment_id = ia.assignment_id " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "WHERE ia.teacher_id = #{teacherId} " +
                        "ORDER BY ir.record_time DESC")
        List<Map<String, Object>> getTeacherInvigilationRecords(@Param("teacherId") Integer teacherId);

        /**
         * 获取考场异常情况汇总
         */
        @Select("SELECT ia.exam_location, " +
                        "COUNT(DISTINCT ir.record_id) as total_records, " +
                        "COUNT(DISTINCT CASE WHEN ir.incident_type IS NOT NULL THEN ir.record_id END) as incident_count, "
                        +
                        "GROUP_CONCAT(DISTINCT ir.incident_type) as incident_types " +
                        "FROM invigilation_record ir " +
                        "JOIN invigilator_assignment ia ON ir.assignment_id = ia.assignment_id " +
                        "WHERE ir.exam_date = #{examDate} " +
                        "GROUP BY ia.exam_location")
        List<Map<String, Object>> getExamRoomIncidentSummary(@Param("examDate") LocalDateTime examDate);

        /**
         * 批量插入监考记录
         */
        @Insert("<script>" +
                        "INSERT INTO invigilation_record (assignment_id, record_time, record_type, " +
                        "incident_type, description, exam_date) VALUES " +
                        "<foreach collection='records' item='record' separator=','>" +
                        "(#{record.assignmentId}, #{record.recordTime}, #{record.recordType}, " +
                        "#{record.incidentType}, #{record.description}, #{record.examDate})" +
                        "</foreach>" +
                        "</script>")
        int batchInsertRecords(@Param("records") List<InvigilationRecord> records);

        /**
         * 获取教师评价信息
         */
        @Select("<script>" +
                        "SELECT e.score, e.assignment_id " +
                        "FROM evaluation e " +
                        "WHERE e.assignment_id IN " +
                        "<foreach collection='list' item='id' separator=',' open='(' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "</script>")
        List<Map<String, Object>> getTeacherEvaluations(List<Long> assignmentIds);
}