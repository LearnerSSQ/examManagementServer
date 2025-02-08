package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.repository.TrainingRecordMapper;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.util.HashMap;

@Service
public class TrainingMaterialServiceImpl extends ServiceImpl<TrainingMaterialMapper, TrainingMaterial>
        implements TrainingMaterialService {

    private static final Logger log = LoggerFactory.getLogger(TrainingMaterialServiceImpl.class);

    @Autowired
    private TrainingMaterialMapper trainingMaterialMapper;

    @Autowired
    private TrainingRecordMapper trainingRecordMapper;

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
    @Transactional
    @CacheEvict(value = "materialCache", key = "#material.materialId")
    public void addTrainingMaterial(TrainingMaterial material) {
        if (material == null) {
            throw new BusinessException("培训材料不能为空");
        }
        material.setCreateTime(LocalDateTime.now());
        material.setStatus(1); // 1: 正常状态
        trainingMaterialMapper.insert(material);
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", key = "#material.materialId")
    public void updateTrainingMaterial(TrainingMaterial material) {
        if (material == null || material.getMaterialId() == null) {
            throw new BusinessException("培训材料信息不完整");
        }
        trainingMaterialMapper.updateById(material);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'latest:' + #limit")
    public List<TrainingMaterial> getLatestMaterials(Integer limit) {
        return trainingMaterialMapper.getLatestMaterials(limit);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'uncompleted:' + #teacherId")
    public List<TrainingMaterial> getUncompletedMaterials(Integer teacherId) {
        return trainingMaterialMapper.getUncompletedMaterials(teacherId);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'stats:' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getMaterialStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return trainingMaterialMapper.getMaterialStatistics(startDate, endDate);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'required'")
    public List<TrainingMaterial> getRequiredMaterials() {
        return trainingMaterialMapper.getRequiredMaterials();
    }

    @Override
    @Cacheable(value = "materialCache", key = "'completion'")
    public List<Map<String, Object>> getMaterialCompletionRates() {
        return trainingMaterialMapper.getMaterialCompletionRates();
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", allEntries = true)
    public void batchUpdateStatus(List<TrainingMaterial> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new BusinessException("培训材料列表不能为空");
        }
        trainingMaterialMapper.batchUpdateStatus(materials);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'deptStats'")
    public List<Map<String, Object>> getDepartmentMaterialStats() {
        return trainingMaterialMapper.getDepartmentMaterialStats();
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", key = "#materialId")
    public boolean updateStatus(Long materialId, Integer status) {
        return trainingMaterialMapper.updateStatus(materialId, status) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", key = "#materialId")
    public boolean updatePassScore(Long materialId, Integer passScore) {
        return trainingMaterialMapper.updatePassScore(materialId, passScore) > 0;
    }

    @Override
    @Cacheable(value = "materialCache", key = "#materialId")
    public TrainingMaterial getMaterialById(Long materialId) {
        return trainingMaterialMapper.selectById(materialId);
    }

    @Override
    public boolean checkMaterialExpiration(Long materialId) {
        TrainingMaterial material = getMaterialById(materialId);
        if (material == null) {
            throw new BusinessException("培训材料不存在");
        }
        return LocalDateTime.now().isAfter(material.getCreateTime().plusDays(30)); // 默认30天过期
    }

    @Override
    @Cacheable(value = "materialCache", key = "'progress:' + #materialId + ':' + #teacherId")
    public Map<String, Object> getMaterialProgress(Long materialId, Integer teacherId) {
        Map<String, Object> progress = new HashMap<>();

        // 获取培训材料信息
        TrainingMaterial material = getMaterialById(materialId);
        if (material == null) {
            throw new BusinessException("培训材料不存在");
        }

        // 获取学习记录
        Map<String, Object> record = trainingRecordMapper.getTeacherTrainingDetails(teacherId).stream()
                .filter(r -> materialId.equals(r.get("material_id")))
                .findFirst()
                .orElse(new HashMap<>());

        progress.put("materialId", materialId);
        progress.put("materialTitle", material.getTitle());
        progress.put("studyTime", record.getOrDefault("study_duration", 0));
        progress.put("quizScore", record.getOrDefault("quiz_score", 0));
        progress.put("status", record.getOrDefault("status", 0));
        progress.put("lastStudyTime", record.get("completion_time"));

        return progress;
    }

    @Override
    @Cacheable(value = "materialCache", key = "'feedback:' + #materialId")
    public Map<String, Object> getMaterialFeedbackStats(Long materialId) {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> materialStats = getMaterialStatistics(null, null);
        Map<String, Object> currentStats = materialStats.stream()
                .filter(s -> materialId.equals(s.get("material_id")))
                .findFirst()
                .orElse(new HashMap<>());

        stats.put("learnerCount", currentStats.getOrDefault("learner_count", 0));
        stats.put("averageScore", currentStats.getOrDefault("average_score", 0));
        stats.put("completedCount", currentStats.getOrDefault("completed_count", 0));

        return stats;
    }

    @Override
    @Cacheable(value = "materialCache", key = "'recommended:' + #teacherId")
    public List<TrainingMaterial> getRecommendedMaterials(Integer teacherId) {
        // 获取教师未完成的材料
        List<TrainingMaterial> uncompletedRequired = trainingMaterialMapper.getUncompletedMaterials(teacherId).stream()
                .filter(m -> m.getType() == 1) // 使用type字段代替isRequired
                .collect(java.util.stream.Collectors.toList());

        if (!uncompletedRequired.isEmpty()) {
            return uncompletedRequired;
        }

        // 如果必修材料都已完成，获取最新的选修材料
        return trainingMaterialMapper.getLatestMaterials(5).stream()
                .filter(m -> m.getType() != 1) // 使用type字段代替isRequired
                .collect(java.util.stream.Collectors.toList());
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