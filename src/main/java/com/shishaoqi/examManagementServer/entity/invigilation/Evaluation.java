package com.shishaoqi.examManagementServer.entity.invigilation;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {

    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long evaluationId;

    /**
     * 监考安排ID
     */
    @TableField("assignment_id")
    private Long assignmentId;

    /**
     * 评价人ID
     */
    @TableField("evaluator_id")
    private Integer evaluatorId;

    /**
     * 评分（支持小数点评分，如90.5）
     */
    @TableField("score")
    private BigDecimal score;

    /**
     * 评价内容
     */
    @TableField("comment")
    private String comment;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}