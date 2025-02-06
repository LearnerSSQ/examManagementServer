package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface InvigilatorAssignmentMapper extends BaseMapper<InvigilatorAssignment> {

    /**
     * 更新监考安排状态
     */
    @Update("UPDATE invigilator_assignment SET status = #{status}, confirm_time = #{confirmTime} WHERE assignment_id = #{assignmentId}")
    int updateStatus(Long assignmentId, Integer status, LocalDateTime confirmTime);

    /**
     * 取消监考安排
     */
    @Update("UPDATE invigilator_assignment SET status = 2 WHERE assignment_id = #{assignmentId}")
    int cancelAssignment(Long assignmentId);
}