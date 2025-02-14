package com.shishaoqi.examManagementServer.entity.invigilation;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

public enum InvigilatorAssignmentRole implements IEnum<String> {
    CHIEF("CHIEF", "主监考"),
    ASSISTANT("ASSISTANT", "副监考");

    @EnumValue
    private final String value;
    private final String description;

    InvigilatorAssignmentRole(String value, String description) {
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