package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TrainingMaterialService extends IService<TrainingMaterial> {

    /**
     * 获取已发布的培训材料列表
     */
    List<TrainingMaterial> getPublishedMaterials();

    /**
     * 获取指定类型的培训材料列表
     */
    List<TrainingMaterial> getMaterialsByType(TrainingMaterialType type);

    /**
     * 更新培训材料状态
     */
    boolean updateStatus(Long materialId, TrainingMaterialStatus status);

    /**
     * 更新考试通过分数
     */
    boolean updatePassScore(Long materialId, Integer passScore);

    /**
     * 添加培训材料
     */
    void addTrainingMaterial(TrainingMaterial material);

    /**
     * 更新培训材料
     */
    void updateTrainingMaterial(TrainingMaterial material);

    /**
     * 获取最新的培训材料
     */
    List<TrainingMaterial> getLatestMaterials(Integer limit);

    /**
     * 获取教师未完成的培训材料
     */
    List<TrainingMaterial> getUncompletedMaterials(Integer teacherId);

    /**
     * 获取培训材料的学习情况统计
     */
    List<Map<String, Object>> getMaterialStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取必修培训材料列表
     */
    List<TrainingMaterial> getRequiredMaterials();

    /**
     * 获取培训材料的完成率统计
     */
    List<Map<String, Object>> getMaterialCompletionRates();

    /**
     * 批量更新培训材料状态
     */
    void batchUpdateStatus(List<TrainingMaterial> materials);

    /**
     * 获取部门培训材料完成情况
     */
    List<Map<String, Object>> getDepartmentMaterialStats();

    /**
     * 获取培训材料详情
     */
    TrainingMaterial getMaterialById(Long materialId);

    /**
     * 检查培训材料是否已过期
     */
    boolean checkMaterialExpiration(Long materialId);

    /**
     * 获取培训材料学习进度
     */
    Map<String, Object> getMaterialProgress(Long materialId, Integer teacherId);

    /**
     * 获取培训材料的反馈统计
     */
    Map<String, Object> getMaterialFeedbackStats(Long materialId);

    /**
     * 获取推荐的培训材料
     */
    List<TrainingMaterial> getRecommendedMaterials(Integer teacherId);
}