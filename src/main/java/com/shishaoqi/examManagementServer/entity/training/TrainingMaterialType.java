package com.shishaoqi.examManagementServer.entity.training;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 培训材料类型枚举
 */
public enum TrainingMaterialType implements IEnum<String> {
    VIDEO("VIDEO", "视频"),
    DOCUMENT("DOCUMENT", "文档"),
    QUIZ("QUIZ", "测验"),
    PRESENTATION("PRESENTATION", "演示文稿");

    @EnumValue
    private final String value;
    private final String description;

    TrainingMaterialType(String value, String description) {
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