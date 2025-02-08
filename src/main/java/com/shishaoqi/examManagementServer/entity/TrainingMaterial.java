package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("training_material")
public class TrainingMaterial {

    /**
     * 培训材料ID
     */
    @TableId(type = IdType.AUTO)
    private Long materialId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 类型：1=文档, 2=视频, 3=测试
     */
    @TableField("type")
    private Integer type;

    /**
     * 预计学习时长（分钟）
     */
    @TableField("required_minutes")
    private Integer requiredMinutes;

    /**
     * 考试题目
     */
    @TableField("exam_questions")
    private String examQuestions;

    /**
     * 通过分数
     */
    @TableField("pass_score")
    private Integer passScore;

    /**
     * 状态：0=草稿, 1=发布, 2=下架
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}