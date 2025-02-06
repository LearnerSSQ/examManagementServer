package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface InvigilatorAssignmentMapper extends BaseMapper<InvigilatorAssignment> {

    /**
     * 更新监考安排状态
     */
    @Update("UPDATE invigilator_assignment SET status = #{status}, confirm_time = #{confirmTime} WHERE assignment_id = #{assignmentId}")
    int updateStatus(@Param("assignmentId") Long assignmentId, @Param("status") Integer status,
            @Param("confirmTime") LocalDateTime confirmTime);

    /**
     * 取消监考安排
     */
    @Update("UPDATE invigilator_assignment SET status = 2 WHERE assignment_id = #{assignmentId}")
    int cancelAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 根据教师ID查询其所有监考安排的ID列表
     *
     * @param teacherId 教师ID
     * @return 监考安排ID列表
     */
    @Select("SELECT assignment_id FROM invigilator_assignment WHERE teacher_id = #{teacherId}")
    List<Long> selectAssignmentIdsByTeacherId(Integer teacherId);
}