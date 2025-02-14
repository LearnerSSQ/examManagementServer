package com.shishaoqi.examManagementServer.entity.invigilation;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监考记录实体类
 * 
 * 该类用于记录监考过程中的各种情况，包括考试秩序、违规情况等。
 * 记录状态包括：草稿、已提交、已审核、已驳回。
 * 系统会自动记录记录的创建时间、提交时间和审核时间。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
@Data
@TableName("invigilation_record")
public class InvigilationRecord {

    /**
     * 监考记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 监考安排ID
     */
    @TableField("assignment_id")
    private Long assignmentId;

    /**
     * 记录类型
     */
    @TableField("type")
    private InvigilationRecordType type;

    /**
     * 记录状态
     */
    @TableField("status")
    private InvigilationRecordStatus status;

    /**
     * 记录内容
     */
    @TableField("content")
    private String content;

    /**
     * 创建者ID（教师ID）
     */
    @TableField("creator_id")
    private Integer creatorId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 审核者ID（教师ID）
     */
    @TableField("reviewer_id")
    private Integer reviewerId;

    /**
     * 审核时间
     */
    @TableField("review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 附件路径（JSON数组）
     */
    @TableField("attachments")
    private String attachments;
}