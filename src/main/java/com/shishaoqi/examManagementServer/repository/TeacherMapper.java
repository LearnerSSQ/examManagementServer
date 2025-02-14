package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

        /**
         * 根据部门查询教师列表
         */
        @Select("SELECT * FROM teacher WHERE department = #{department}")
        List<Teacher> selectByDepartment(@Param("department") String department);

        /**
         * 根据邮箱查询教师
         */
        @Select("SELECT * FROM teacher WHERE email = #{email}")
        Teacher selectByEmail(@Param("email") String email);

        /**
         * 根据手机号查询教师
         */
        @Select("SELECT * FROM teacher WHERE phone = #{phone}")
        Teacher selectByPhone(@Param("phone") String phone);

        @Update("UPDATE teacher SET status = #{status} WHERE teacher_id = #{teacherId}")
        int updateStatus(@Param("teacherId") Integer teacherId, @Param("status") TeacherStatus status);

        @Update("UPDATE teacher SET last_login = #{lastLogin} WHERE teacher_id = #{teacherId}")
        int updateLastLogin(@Param("teacherId") Integer teacherId, @Param("lastLogin") LocalDateTime lastLogin);

        @Update("UPDATE teacher SET title = #{title} WHERE teacher_id = #{teacherId}")
        int updateTitle(@Param("teacherId") Integer teacherId, @Param("title") String title);

        /**
         * 获取可用于监考的教师列表
         */
        @Select("SELECT t.*, " +
                        "COUNT(DISTINCT ia.assignment_id) as total_assignments, " +
                        "AVG(e.score) as average_score " +
                        "FROM teacher t " +
                        "LEFT JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "WHERE t.status = 'ACTIVE' " +
                        "GROUP BY t.teacher_id " +
                        "HAVING (average_score >= #{minScore} OR average_score IS NULL) " +
                        "AND (total_assignments < #{maxAssignments} OR total_assignments IS NULL)")
        List<Map<String, Object>> getAvailableInvigilators(
                        @Param("minScore") Double minScore,
                        @Param("maxAssignments") Integer maxAssignments);

        /**
         * 获取教师培训完成状态
         */
        @Select("SELECT t.*, " +
                        "COUNT(DISTINCT tr.material_id) as completed_trainings, " +
                        "(SELECT COUNT(*) FROM training_material) as total_trainings, " +
                        "MAX(tr.completion_time) as last_training_time " +
                        "FROM teacher t " +
                        "LEFT JOIN training_record tr ON t.teacher_id = tr.teacher_id " +
                        "WHERE t.department = #{department} " +
                        "GROUP BY t.teacher_id")
        List<Map<String, Object>> getTeacherTrainingStatus(@Param("department") String department);

        /**
         * 获取教师监考经验统计
         */
        @Select("SELECT t.teacher_id, t.name, t.department, " +
                        "COUNT(DISTINCT ia.assignment_id) as total_invigilation, " +
                        "COUNT(DISTINCT CASE WHEN e.score >= 90 THEN ia.assignment_id END) as excellent_count, " +
                        "AVG(e.score) as average_score " +
                        "FROM teacher t " +
                        "LEFT JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "WHERE ia.exam_date BETWEEN #{startDate} AND #{endDate} " +
                        "GROUP BY t.teacher_id, t.name, t.department")
        List<Map<String, Object>> getTeacherExperienceStats(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * 获取教师考试时间冲突检查
         */
        @Select("SELECT t.teacher_id, t.name, " +
                        "ia.exam_date, ia.exam_location " +
                        "FROM teacher t " +
                        "JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "WHERE t.teacher_id = #{teacherId} " +
                        "AND ia.exam_date BETWEEN #{startDate} AND #{endDate}")
        List<Map<String, Object>> checkTimeConflicts(
                        @Param("teacherId") Integer teacherId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * 获取部门监考任务统计
         */
        @Select("SELECT t.department, " +
                        "COUNT(DISTINCT t.teacher_id) as total_teachers, " +
                        "COUNT(DISTINCT ia.assignment_id) as total_assignments, " +
                        "AVG(e.score) as average_score " +
                        "FROM teacher t " +
                        "LEFT JOIN invigilator_assignment ia ON t.teacher_id = ia.teacher_id " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "GROUP BY t.department")
        List<Map<String, Object>> getDepartmentWorkloadStats();

        /**
         * 更新教师状态
         */
        @Update("UPDATE teacher SET status = #{status}, " +
                        "update_time = #{updateTime}, " +
                        "update_reason = #{reason} " +
                        "WHERE teacher_id = #{teacherId}")
        int updateTeacherStatus(
                        @Param("teacherId") Integer teacherId,
                        @Param("status") Integer status,
                        @Param("updateTime") LocalDateTime updateTime,
                        @Param("reason") String reason);

        /**
         * 批量更新教师信息
         */
        @Update("<script>" +
                        "<foreach collection='teachers' item='teacher' separator=';'>" +
                        "UPDATE teacher SET " +
                        "name = #{teacher.name}, " +
                        "department = #{teacher.department}, " +
                        "phone = #{teacher.phone}, " +
                        "email = #{teacher.email}, " +
                        "status = #{teacher.status}, " +
                        "update_time = NOW() " +
                        "WHERE teacher_id = #{teacher.teacherId}" +
                        "</foreach>" +
                        "</script>")
        int batchUpdateTeachers(@Param("teachers") List<Teacher> teachers);

        /**
         * 更新教师角色
         */
        @Update("UPDATE teacher SET role = #{role} WHERE teacher_id = #{teacherId}")
        int updateRole(@Param("teacherId") Integer teacherId, @Param("role") TeacherRole role);

        /**
         * 根据角色查询教师列表
         */
        @Select("SELECT * FROM teacher WHERE role = #{role}")
        List<Teacher> selectByRole(@Param("role") TeacherRole role);

        /**
         * 检查教师是否具有指定角色
         */
        @Select("SELECT COUNT(*) FROM teacher WHERE teacher_id = #{teacherId} AND role = #{role}")
        int checkRole(@Param("teacherId") Integer teacherId, @Param("role") TeacherRole role);
}