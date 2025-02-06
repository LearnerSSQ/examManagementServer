package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long evaluationId;

    private Long assignmentId;

    private Integer evaluatorId;

    private BigDecimal score;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}