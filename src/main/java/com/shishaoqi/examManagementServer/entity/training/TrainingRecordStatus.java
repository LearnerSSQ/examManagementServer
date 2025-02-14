package com.shishaoqi.examManagementServer.entity.training;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

public enum TrainingRecordStatus implements IEnum<String> {
    NOT_STARTED("NOT_STARTED", "未开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    EXPIRED("EXPIRED", "已过期");

    @EnumValue
    private final String value;
    private final String description;

    TrainingRecordStatus(String value, String description) {
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