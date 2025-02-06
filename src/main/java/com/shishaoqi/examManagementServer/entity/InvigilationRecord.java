package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invigilation_record")
public class InvigilationRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long assignmentId;

    /**
     * 1=签到, 2=异常事件, 3=备注
     */
    private Integer type;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}