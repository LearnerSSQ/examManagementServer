package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {

    @TableId(type = IdType.AUTO)
    private Integer teacherId;

    private String name;

    private String phone;

    private String email;

    private String department;

    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}