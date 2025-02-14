package com.shishaoqi.examManagementServer.entity.training;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 培训记录实体类
 * 
 * 该类用于记录教师的培训学习情况，包括学习进度、状态和时间等信息。
 * 培训状态包括未开始、进行中、已完成和已过期。
 * 系统会自动记录教师的学习进度和最后访问时间。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
@Data
@TableName("training_record")
public class TrainingRecord {

    /**
     * 培训记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 教师ID
     */
    @TableField("teacher_id")
    private Integer teacherId;

    /**
     * 培训材料ID
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 开始学习时间
     */
    @TableField(value = "start_time", fill = FieldFill.INSERT)
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 培训记录状态
     */
    @TableField("status")
    private TrainingRecordStatus status;

    /**
     * 学习进度（百分比）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 最后访问时间
     */
    @TableField("last_access")
    private LocalDateTime lastAccess;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

    public TrainingRecord() {
        this.progress = 0;
        this.status = TrainingRecordStatus.NOT_STARTED;
        this.startTime = LocalDateTime.now();
    }
}