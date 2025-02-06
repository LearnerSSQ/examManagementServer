package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long messageId;

    private Integer teacherId;

    private String title;

    private String content;

    /**
     * 1=系统通知, 2=监考提醒, 3=培训通知
     */
    private Integer type;

    /**
     * 0=未读, 1=已读
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime readTime;
}