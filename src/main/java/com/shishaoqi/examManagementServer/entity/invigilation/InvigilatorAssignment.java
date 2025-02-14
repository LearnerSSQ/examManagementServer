package com.shishaoqi.examManagementServer.entity.invigilation;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监考安排实体类
 * 
 * 该类用于管理教师的监考任务安排，包括考试信息、监考角色和状态等。
 * 监考角色分为主监考和副监考，状态包括待确认、已确认、已完成和已取消。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
@Data
@TableName("invigilator_assignment")
public class InvigilatorAssignment {

    /**
     * 监考安排ID
     */
    @TableId(type = IdType.AUTO)
    private Long assignmentId;

    /**
     * 教师ID
     */
    @TableField("teacher_id")
    private Integer teacherId;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 考试开始时间
     */
    @TableField("exam_start")
    private LocalDateTime examStart;

    /**
     * 考试结束时间
     */
    @TableField("exam_end")
    private LocalDateTime examEnd;

    /**
     * 考试地点
     */
    @TableField("location")
    private String location;

    /**
     * 监考角色
     */
    @TableField("role")
    private InvigilatorAssignmentRole role;

    /**
     * 监考任务状态
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private InvigilatorAssignmentStatus status;

    /**
     * 分配时间
     */
    @TableField(value = "assign_time", fill = FieldFill.INSERT)
    private LocalDateTime assignTime;

    /**
     * 确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;
}