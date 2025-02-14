package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialType;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
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
        wrapper.eq(TrainingMaterial::getStatus, TrainingMaterialStatus.PUBLISHED)
                .orderByDesc(TrainingMaterial::getCreateTime);
        List<TrainingMaterial> materials = list(wrapper);
        log.info("获取已发布的培训材料列表，共{}条", materials.size());
        return materials;
    }

    @Override
    public List<TrainingMaterial> getMaterialsByType(TrainingMaterialType type) {
        if (type == null) {
            log.error("获取培训材料失败：类型为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getType, type)
                .eq(TrainingMaterial::getStatus, TrainingMaterialStatus.PUBLISHED)
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
        material.setStatus(TrainingMaterialStatus.PUBLISHED);
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
        if (limit == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return trainingMaterialMapper.getLatestMaterials(TrainingMaterialStatus.PUBLISHED, limit);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'uncompleted:' + #teacherId")
    public List<TrainingMaterial> getUncompletedMaterials(Integer teacherId) {
        if (teacherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return trainingMaterialMapper.getUncompletedMaterials(teacherId, TrainingMaterialStatus.PUBLISHED);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'stats:' + #startDate + ':' + #endDate")
    public List<Map<String, Object>> getMaterialStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return trainingMaterialMapper.getMaterialStatistics(startDate, endDate, TrainingRecordStatus.COMPLETED);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'completion'")
    public List<Map<String, Object>> getMaterialCompletionRates() {
        return trainingMaterialMapper.getMaterialCompletionRates(TrainingRecordStatus.COMPLETED,
                TrainingMaterialStatus.PUBLISHED);
    }

    @Override
    @Cacheable(value = "materialCache", key = "'deptStats'")
    public List<Map<String, Object>> getDepartmentMaterialStats() {
        return trainingMaterialMapper.getDepartmentMaterialStats(TrainingRecordStatus.COMPLETED);
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", key = "#materialId")
    public boolean updateStatus(Long materialId, TrainingMaterialStatus status) {
        if (materialId == null || status == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return trainingMaterialMapper.updateStatus(materialId, status) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = "materialCache", key = "#materialId")
    public boolean updatePassScore(Long materialId, Integer passScore) {
        if (materialId == null || passScore == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
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
    @Transactional
    @CacheEvict(value = "materialCache", allEntries = true)
    public void batchUpdateStatus(List<TrainingMaterial> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new BusinessException("培训材料列表不能为空");
        }
        trainingMaterialMapper.batchUpdateStatus(materials);
    }

    @Override
    public List<TrainingMaterial> getRecommendedMaterials(Integer teacherId) {
        // 获取所有已发布的培训材料
        LambdaQueryWrapper<TrainingMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingMaterial::getStatus, TrainingMaterialStatus.PUBLISHED)
                .orderByDesc(TrainingMaterial::getCreateTime);

        // 获取教师未完成的必修材料
        List<TrainingMaterial> uncompletedRequired = list(wrapper.clone()
                .eq(TrainingMaterial::getIsRequired, true)
                .notExists("SELECT 1 FROM training_record tr WHERE tr.material_id = training_material.material_id " +
                        "AND tr.teacher_id = {0} AND tr.status = {1}", teacherId, TrainingRecordStatus.COMPLETED));

        if (!uncompletedRequired.isEmpty()) {
            return uncompletedRequired;
        }

        // 如果必修材料都已完成，获取最新的选修材料
        return list(wrapper.clone()
                .eq(TrainingMaterial::getIsRequired, false)
                .last("LIMIT 5"));
    }

    @Override
    @Cacheable(value = "materialCache", key = "'required'")
    public List<TrainingMaterial> getRequiredMaterials() {
        return trainingMaterialMapper.getRequiredMaterials(TrainingMaterialStatus.PUBLISHED);
    }
}