package com.shishaoqi.examManagementServer.entity.teacher;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

public enum TeacherStatus implements IEnum<String> {
    INACTIVE("INACTIVE", "未激活"),
    ACTIVE("ACTIVE", "已激活"),
    DISABLED("DISABLED", "已停用");

    @EnumValue
    private final String value;
    private final String description;

    TeacherStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }
}