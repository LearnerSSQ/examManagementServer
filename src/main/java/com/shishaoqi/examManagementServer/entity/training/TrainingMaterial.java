package com.shishaoqi.examManagementServer.entity.training;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 培训材料实体类
 * 
 * 该类用于管理培训材料信息，包括材料内容、类型、状态等。
 * 材料类型包括：视频、文档、测验、演示文稿。
 * 材料状态包括：草稿、已发布、已归档、已删除。
 * 系统会自动记录材料的创建时间。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
@Data
@TableName("training_material")
public class TrainingMaterial {

    /**
     * 培训材料ID
     */
    @TableId(type = IdType.AUTO)
    private Long materialId;

    /**
     * 材料标题
     */
    @TableField("title")
    private String title;

    /**
     * 材料描述
     */
    @TableField("description")
    private String description;

    /**
     * 材料类型
     */
    @TableField("type")
    private TrainingMaterialType type;

    /**
     * 材料状态
     */
    @TableField("status")
    private TrainingMaterialStatus status;

    /**
     * 材料内容（文档类型存储路径，测验类型存储JSON）
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
     * 学习时长（分钟）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 是否必修
     */
    @TableField("is_required")
    private Boolean isRequired;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    private String tags;
}