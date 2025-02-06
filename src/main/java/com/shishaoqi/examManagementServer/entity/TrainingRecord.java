package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_record")
public class TrainingRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long materialId;

    private Integer teacherId;

    /**
     * 实际学习时长（分钟）
     */
    private Integer studyTime;

    private Integer examScore;

    /**
     * 0=未开始, 1=进行中, 2=已完成
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;

    private LocalDateTime completeTime;
}