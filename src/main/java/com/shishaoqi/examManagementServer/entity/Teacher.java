package com.shishaoqi.examManagementServer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {

    /**
     * 教师ID
     */
    @TableId(type = IdType.AUTO)
    private Integer teacherId;

    /**
     * 教师姓名
     */
    @TableField("name")
    private String name;

    /**
     * 密码（BCrypt加密后的哈希值）
     */
    @TableField("password")
    private String password;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱（唯一）
     */
    @TableField("email")
    private String email;

    /**
     * 所属部门
     */
    @TableField("department")
    private String department;

    /**
     * 职称
     */
    @TableField("title")
    private String title;

    /**
     * 状态：0=未激活, 1=已激活, 2=已停用
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login")
    private LocalDateTime lastLogin;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}