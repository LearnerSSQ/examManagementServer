package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;

@Service
public class TrainingMaterialServiceImpl extends ServiceImpl<TrainingMaterialMapper, TrainingMaterial>
        implements TrainingMaterialService {

    private static final Logger log = LoggerFactory.getLogger(TrainingMaterialServiceImpl.class);

    @Override
    public List<TrainingMaterial> getPublishedMaterials() {
        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getStatus, 1) // 已发布状态
                .orderByDesc(TrainingMaterial::getCreateTime);
        List<TrainingMaterial> materials = list(wrapper);
        log.info("获取已发布的培训材料列表，共{}条", materials.size());
        return materials;
    }

    @Override
    public List<TrainingMaterial> getMaterialsByType(Integer type) {
        if (type == null || type < 1 || type > 3) {
            log.error("获取培训材料失败：类型无效，type={}", type);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getType, type)
                .eq(TrainingMaterial::getStatus, 1) // 只查询已发布的
                .orderByDesc(TrainingMaterial::getCreateTime);
        List<TrainingMaterial> materials = list(wrapper);
        log.info("获取类型[{}]的培训材料列表，共{}条", type, materials.size());
        return materials;
    }

    @Override
    public boolean updateStatus(Long materialId, Integer status) {
        if (materialId == null) {
            log.error("更新培训材料状态失败：材料ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (status == null || status < 0 || status > 2) {
            log.error("更新培训材料状态失败：状态无效，status={}", status);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 检查材料是否存在
        TrainingMaterial material = getById(materialId);
        if (material == null) {
            log.error("更新培训材料状态失败：材料不存在，ID={}", materialId);
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        boolean success = baseMapper.updateStatus(materialId, status) > 0;
        if (success) {
            log.info("成功更新培训材料状态，ID：{}，新状态：{}", materialId, status);
        } else {
            log.error("更新培训材料状态失败，ID：{}", materialId);
        }
        return success;
    }

    @Override
    public boolean updatePassScore(Long materialId, Integer passScore) {
        if (materialId == null) {
            log.error("更新通过分数失败：材料ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (passScore == null || passScore < 0 || passScore > 100) {
            log.error("更新通过分数失败：分数无效，passScore={}", passScore);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 检查材料是否存在
        TrainingMaterial material = getById(materialId);
        if (material == null) {
            log.error("更新通过分数失败：材料不存在，ID={}", materialId);
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        boolean success = baseMapper.updatePassScore(materialId, passScore) > 0;
        if (success) {
            log.info("成功更新培训材料通过分数，ID：{}，新分数：{}", materialId, passScore);
        } else {
            log.error("更新培训材料通过分数失败，ID：{}", materialId);
        }
        return success;
    }

    @Override
    public boolean save(TrainingMaterial material) {
        if (material == null) {
            log.error("保存培训材料失败：材料对象为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (material.getTitle() == null || material.getTitle().trim().isEmpty()) {
            log.error("保存培训材料失败：标题为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (material.getType() == null || material.getType() < 1 || material.getType() > 3) {
            log.error("保存培训材料失败：类型无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (material.getRequiredMinutes() != null && material.getRequiredMinutes() < 0) {
            log.error("保存培训材料失败：要求学习时长无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 设置初始状态为草稿
        material.setStatus(0);
        material.setCreateTime(LocalDateTime.now());

        boolean success = super.save(material);
        if (success) {
            log.info("成功保存培训材料，ID：{}，标题：{}", material.getMaterialId(), material.getTitle());
        } else {
            log.error("保存培训材料失败");
        }
        return success;
    }

    @Override
    public boolean updateById(TrainingMaterial material) {
        if (material == null || material.getMaterialId() == null) {
            log.error("更新培训材料失败：材料ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 检查材料是否存在
        TrainingMaterial existingMaterial = getById(material.getMaterialId());
        if (existingMaterial == null) {
            log.error("更新培训材料失败：材料不存在，ID={}", material.getMaterialId());
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (material.getType() != null && (material.getType() < 1 || material.getType() > 3)) {
            log.error("更新培训材料失败：类型无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (material.getRequiredMinutes() != null && material.getRequiredMinutes() < 0) {
            log.error("更新培训材料失败：要求学习时长无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        boolean success = super.updateById(material);
        if (success) {
            log.info("成功更新培训材料，ID：{}", material.getMaterialId());
        } else {
            log.error("更新培训材料失败，ID：{}", material.getMaterialId());
        }
        return success;
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            log.error("删除培训材料失败：材料ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 检查材料是否存在
        TrainingMaterial existingMaterial = getById(id);
        if (existingMaterial == null) {
            log.error("删除培训材料失败：材料不存在，ID={}", id);
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        boolean success = super.removeById(id);
        if (success) {
            log.info("成功删除培训材料，ID：{}", id);
        } else {
            log.error("删除培训材料失败，ID：{}", id);
        }
        return success;
    }
}