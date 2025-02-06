package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invigilation_record")
public class InvigilationRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 监考安排ID
     */
    @TableField("assignment_id")
    private Long assignmentId;

    /**
     * 记录类型：1=签到, 2=异常事件, 3=备注
     */
    @TableField("type")
    private Integer type;

    /**
     * 记录描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}