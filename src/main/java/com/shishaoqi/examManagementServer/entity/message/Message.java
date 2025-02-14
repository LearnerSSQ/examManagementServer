package com.shishaoqi.examManagementServer.entity.message;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 
 * 该类用于管理系统内的消息通知，包括系统消息、监考安排通知、培训通知等。
 * 消息类型包括：系统消息、监考安排、培训通知、一般通知。
 * 消息状态包括：未读、已读、已归档。
 * 系统会自动记录消息的发送时间和阅读时间。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
@Data
@TableName("message")
public class Message {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long messageId;

    /**
     * 消息标题
     */
    @TableField("title")
    private String title;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型
     */
    @TableField("type")
    private MessageType type;

    /**
     * 消息状态
     */
    @TableField("status")
    private MessageStatus status;

    /**
     * 接收者ID（教师ID）
     */
    @TableField("receiver_id")
    private Integer receiverId;

    /**
     * 发送者ID（教师ID，0表示系统发送）
     */
    @TableField("sender_id")
    private Integer senderId;

    /**
     * 发送时间
     */
    @TableField(value = "send_time", fill = FieldFill.INSERT)
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 相关业务ID（如监考安排ID、培训记录ID等）
     */
    @TableField("reference_id")
    private Long referenceId;

    /**
     * 是否需要确认
     */
    @TableField("require_confirm")
    private Boolean requireConfirm;

    /**
     * 确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;
}