package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invigilator_assignment")
public class InvigilatorAssignment {

    @TableId(type = IdType.AUTO)
    private Long assignmentId;

    private Integer teacherId;

    private String courseName;

    private LocalDateTime examStart;

    private LocalDateTime examEnd;

    private String location;

    /**
     * 0=主监考, 1=副监考
     */
    private Integer role;

    /**
     * 0=未确认, 1=已确认, 2=已取消
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime assignTime;

    private LocalDateTime confirmTime;
}