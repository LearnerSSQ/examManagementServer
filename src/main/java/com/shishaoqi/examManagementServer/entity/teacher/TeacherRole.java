package com.shishaoqi.examManagementServer.entity.teacher;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

public enum TeacherRole implements IEnum<String> {
    ADMIN("ADMIN"), // 系统管理员
    EXAM_ADMIN("EXAM_ADMIN"), // 考务管理员
    TEACHER("TEACHER"); // 普通教师

    @EnumValue
    private final String value;

    TeacherRole(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}