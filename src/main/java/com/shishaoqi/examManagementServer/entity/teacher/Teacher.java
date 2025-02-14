package com.shishaoqi.examManagementServer.entity.teacher;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教师实体类
 * 
 * 该类用于管理教师的基本信息，包括个人信息、账号状态和系统角色等。
 * 教师状态包括未激活、已激活和已停用。
 * 系统角色包括系统管理员、考务管理员和普通教师。
 * 
 * @author shishaoqi
 * @since 2024-01-01
 */
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
     * 教师状态
     */
    @TableField(value = "status", fill = FieldFill.INSERT)
    private TeacherStatus status;

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

    /**
     * 教师角色：ADMIN=系统管理员, EXAM_ADMIN=考务管理员, TEACHER=普通教师
     */
    @TableField("role")
    private TeacherRole role;
}