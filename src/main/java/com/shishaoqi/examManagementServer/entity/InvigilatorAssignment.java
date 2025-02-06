package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

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
     * 监考角色：0=主监考, 1=副监考
     */
    @TableField("role")
    private Integer role;

    /**
     * 状态：0=未确认, 1=已确认, 2=已取消
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private Integer status;

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