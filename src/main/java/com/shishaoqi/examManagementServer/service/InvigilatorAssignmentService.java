package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import java.time.LocalDateTime;
import java.util.List;

public interface InvigilatorAssignmentService extends IService<InvigilatorAssignment> {

    /**
     * 获取教师的监考安排列表
     */
    List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId);

    /**
     * 获取时间段内的监考安排
     */
    List<InvigilatorAssignment> getAssignmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新监考安排状态
     */
    boolean updateStatus(Long assignmentId, Integer status);

    /**
     * 取消监考安排
     */
    boolean cancelAssignment(Long assignmentId);
}