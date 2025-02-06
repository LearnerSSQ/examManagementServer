package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_material")
public class TrainingMaterial {

    @TableId(type = IdType.AUTO)
    private Long materialId;

    private String title;

    private String description;

    private String content;

    /**
     * 1=文档, 2=视频, 3=测试
     */
    private Integer type;

    /**
     * 预计学习时长（分钟）
     */
    private Integer requiredMinutes;

    private String examQuestions;

    private Integer passScore;

    /**
     * 0=草稿, 1=发布, 2=下架
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}