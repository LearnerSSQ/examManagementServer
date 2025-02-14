package com.shishaoqi.examManagementServer.entity.training;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 培训材料状态枚举
 */
public enum TrainingMaterialStatus implements IEnum<String> {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ARCHIVED("ARCHIVED", "已归档"),
    DELETED("DELETED", "已删除");

    @EnumValue
    private final String value;
    private final String description;

    TrainingMaterialStatus(String value, String description) {
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