package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import java.util.List;

public interface TrainingMaterialService extends IService<TrainingMaterial> {

    /**
     * 获取已发布的培训材料列表
     */
    List<TrainingMaterial> getPublishedMaterials();

    /**
     * 获取指定类型的培训材料列表
     */
    List<TrainingMaterial> getMaterialsByType(Integer type);

    /**
     * 更新培训材料状态
     */
    boolean updateStatus(Long materialId, Integer status);

    /**
     * 更新考试通过分数
     */
    boolean updatePassScore(Long materialId, Integer passScore);
}