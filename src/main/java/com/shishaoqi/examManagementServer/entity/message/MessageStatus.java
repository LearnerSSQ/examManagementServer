package com.shishaoqi.examManagementServer.entity.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 消息状态枚举
 */
public enum MessageStatus implements IEnum<String> {
    UNREAD("UNREAD", "未读"),
    READ("READ", "已读"),
    ARCHIVED("ARCHIVED", "已归档");

    @EnumValue
    private final String value;
    private final String description;

    MessageStatus(String value, String description) {
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