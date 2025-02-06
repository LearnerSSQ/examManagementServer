package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 培训材料ID
     */
    @TableField("material_id")
    private Long materialId;

    /**
     * 教师ID
     */
    @TableField("teacher_id")
    private Integer teacherId;

    /**
     * 实际学习时长（分钟）
     */
    @TableField(value = "study_time", fill = FieldFill.INSERT)
    private Integer studyTime;

    /**
     * 考试得分，未考试时为null
     */
    @TableField("exam_score")
    private Integer examScore;

    /**
     * 状态：0=未开始, 1=进行中, 2=已完成
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private Integer status;

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

    public TrainingRecord() {
        this.studyTime = 0;
        this.status = 0;
        this.startTime = LocalDateTime.now();
    }
}