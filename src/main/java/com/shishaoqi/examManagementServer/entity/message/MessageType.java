package com.shishaoqi.examManagementServer.entity.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 消息类型枚举
 */
public enum MessageType implements IEnum<String> {
    SYSTEM("SYSTEM", "系统消息"),
    ASSIGNMENT("ASSIGNMENT", "监考安排"),
    TRAINING("TRAINING", "培训通知"),
    NOTIFICATION("NOTIFICATION", "一般通知");

    @EnumValue
    private final String value;
    private final String description;

    MessageType(String value, String description) {
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