package com.shishaoqi.examManagementServer.entity.invigilation;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 监考记录类型枚举
 */
public enum InvigilationRecordType implements IEnum<String> {
    SIGN_IN("SIGN_IN", "签到记录"),
    INCIDENT("INCIDENT", "异常事件"),
    VIOLATION("VIOLATION", "违规记录"),
    NOTE("NOTE", "一般备注");

    @EnumValue
    private final String value;
    private final String description;

    InvigilationRecordType(String value, String description) {
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