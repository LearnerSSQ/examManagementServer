package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import java.util.List;

public interface TrainingMaterialService extends IService<TrainingMaterial> {

    /**
     * 获取所有已发布的培训材料
     */
    List<TrainingMaterial> getPublishedMaterials();

    /**
     * 根据类型获取培训材料列表
     */
    List<TrainingMaterial> getMaterialsByType(Integer type);
}