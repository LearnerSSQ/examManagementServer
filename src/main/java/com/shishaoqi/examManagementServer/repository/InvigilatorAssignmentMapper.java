package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InvigilatorAssignmentMapper extends BaseMapper<InvigilatorAssignment> {

        /**
         * 更新监考安排状态
         */
        @Update("UPDATE invigilator_assignment SET status = #{status}, confirm_time = #{confirmTime} WHERE assignment_id = #{assignmentId}")
        int updateStatus(@Param("assignmentId") Long assignmentId, @Param("status") InvigilatorAssignmentStatus status,
                        @Param("confirmTime") LocalDateTime confirmTime);

        /**
         * 取消监考安排
         */
        @Update("UPDATE invigilator_assignment SET status = 'CANCELLED' WHERE assignment_id = #{assignmentId}")
        int cancelAssignment(@Param("assignmentId") Long assignmentId);

        /**
         * 根据教师ID查询其所有监考安排的ID列表
         */
        @Select("SELECT assignment_id FROM invigilator_assignment WHERE teacher_id = #{teacherId}")
        List<Long> selectAssignmentIdsByTeacherId(@Param("teacherId") Integer teacherId);

        /**
         * 获取待评价的监考安排
         */
        @Select("SELECT ia.* FROM invigilator_assignment ia " +
                        "LEFT JOIN evaluation e ON ia.assignment_id = e.assignment_id " +
                        "AND e.evaluator_id = #{evaluatorId} " +
                        "WHERE ia.status = 1 " + // 已确认的监考安排
                        "AND ia.exam_end < NOW() " + // 已结束的考试
                        "AND e.evaluation_id IS NULL " + // 尚未评价
                        "ORDER BY ia.exam_end DESC")
        List<InvigilatorAssignment> selectPendingEvaluations(@Param("evaluatorId") Integer evaluatorId);
}