package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrainingMaterialServiceImpl extends ServiceImpl<TrainingMaterialMapper, TrainingMaterial>
        implements TrainingMaterialService {

    @Override
    public List<TrainingMaterial> getPublishedMaterials() {
        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getStatus, 1) // 已发布状态
                .orderByDesc(TrainingMaterial::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<TrainingMaterial> getMaterialsByType(Integer type) {
        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getType, type)
                .eq(TrainingMaterial::getStatus, 1) // 只查询已发布的
                .orderByDesc(TrainingMaterial::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean updateStatus(Long materialId, Integer status) {
        return baseMapper.updateStatus(materialId, status) > 0;
    }

    @Override
    public boolean updatePassScore(Long materialId, Integer passScore) {
        return baseMapper.updatePassScore(materialId, passScore) > 0;
    }
}