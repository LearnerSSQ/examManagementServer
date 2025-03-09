package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialStatus;
import com.shishaoqi.examManagementServer.entity.training.TrainingMaterialType;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import com.shishaoqi.examManagementServer.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TrainingMaterialServiceImpl extends ServiceImpl<TrainingMaterialMapper, TrainingMaterial>
        implements TrainingMaterialService {

    @Autowired
    private TrainingMaterialMapper trainingMaterialMapper;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Override
    public List<TrainingMaterial> getPublishedMaterials() {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.eq("status", TrainingMaterialStatus.PUBLISHED);
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public List<TrainingMaterial> getMaterialsByType(TrainingMaterialType type) {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.eq("type", type);
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public boolean updateStatus(Long materialId, TrainingMaterialStatus status) {
        TrainingMaterial material = trainingMaterialMapper.selectById(materialId);
        if (material != null) {
            material.setStatus(status);
            return trainingMaterialMapper.updateById(material) > 0;
        }
        return false;
    }

    @Override
    public boolean updatePassScore(Long materialId, Integer passScore) {
        TrainingMaterial material = trainingMaterialMapper.selectById(materialId);
        if (material != null) {
            material.setPassScore(passScore);
            return trainingMaterialMapper.updateById(material) > 0;
        }
        return false;
    }

    @Override
    public void addTrainingMaterial(TrainingMaterial material) {
        trainingMaterialMapper.insert(material);
    }

    @Override
    public void updateTrainingMaterial(TrainingMaterial material) {
        trainingMaterialMapper.updateById(material);
    }

    @Override
    public List<TrainingMaterial> getLatestMaterials(Integer limit) {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        query.last("LIMIT " + limit);
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public List<TrainingMaterial> getUncompletedMaterials(Integer teacherId) {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.eq("status", TrainingMaterialStatus.PUBLISHED);
        query.notExists(
                "SELECT 1 FROM training_record tr WHERE tr.material_id = material_id AND tr.teacher_id = " + teacherId);
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public List<Map<String, Object>> getMaterialStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return trainingMaterialMapper.getMaterialStatistics(startDate, endDate, TrainingRecordStatus.COMPLETED);
    }

    @Override
    public List<TrainingMaterial> getRequiredMaterials() {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.eq("is_required", true);
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public List<Map<String, Object>> getMaterialCompletionRates() {
        return trainingMaterialMapper.getMaterialCompletionRates(TrainingRecordStatus.COMPLETED,
                TrainingMaterialStatus.PUBLISHED);
    }

    @Override
    public void batchUpdateStatus(List<TrainingMaterial> materials) {
        materials.forEach(material -> trainingMaterialMapper.updateById(material));
    }

    @Override
    public List<Map<String, Object>> getDepartmentMaterialStats() {
        return trainingMaterialMapper.getDepartmentMaterialStats(TrainingRecordStatus.COMPLETED);
    }

    @Override
    public TrainingMaterial getMaterialById(Long materialId) {
        return trainingMaterialMapper.selectById(materialId);
    }

    @Override
    public boolean checkMaterialExpiration(Long materialId) {
        TrainingMaterial material = trainingMaterialMapper.selectById(materialId);
        if (material != null && material.getExpireDate() != null) {
            return material.getExpireDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public Map<String, Object> getMaterialProgress(Long materialId, Integer teacherId) {
        return trainingMaterialMapper.selectMaterialProgress(materialId, teacherId);
    }

    @Override
    public Map<String, Object> getMaterialFeedbackStats(Long materialId) {
        return trainingMaterialMapper.selectMaterialFeedbackStats(materialId);
    }

    @Override
    public List<TrainingMaterial> getRecommendedMaterials(Integer teacherId) {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.eq("status", TrainingMaterialStatus.PUBLISHED);
        query.notExists(
                "SELECT 1 FROM training_record tr WHERE tr.material_id = material_id AND tr.teacher_id = " + teacherId);
        query.orderByDesc("is_required", "create_time");
        return trainingMaterialMapper.selectList(query);
    }

    @Override
    public TrainingMaterial createMaterial(TrainingMaterial material, MultipartFile file) throws IOException {
        // 先保存培训材料信息，以获取materialId
        material.setContent("");
        trainingMaterialMapper.insert(material);

        // 从数据库中重新获取material对象，确保有materialId
        material = trainingMaterialMapper.selectById(material.getMaterialId());

        if (file != null && !file.isEmpty()) {
            if (!fileStorageUtil.isValidFileType(file)) {
                throw new IllegalArgumentException("不支持的文件类型");
            }

            // 存储文件
            String filePath = fileStorageUtil.storeFile(file, material);
            material.setContent(fileStorageUtil.getFileAccessUrl(filePath));

            // 更新材料内容路径
            trainingMaterialMapper.updateById(material);
        }
        return material;
    }
    @Override
    public void deleteMaterial(Long materialId) throws IOException {
        TrainingMaterial material = trainingMaterialMapper.selectById(materialId);
        if (material != null) {
            // 删除文件
            String filePath = material.getContent();
            if (filePath != null && !filePath.isEmpty()) {
                fileStorageUtil.deleteFile(filePath);
            }

            // 删除数据库记录
            trainingMaterialMapper.deleteById(materialId);
        }
    }

    @Override
    public TrainingMaterial updateMaterial(TrainingMaterial material, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            // 验证文件类型
            if (!fileStorageUtil.isValidFileType(file)) {
                throw new IllegalArgumentException("不支持的文件类型");
            }

            // 删除旧文件
            String oldFilePath = material.getContent();
            if (oldFilePath != null && !oldFilePath.isEmpty()) {
                fileStorageUtil.deleteFile(oldFilePath);
            }

            // 存储新文件
            String newFilePath = fileStorageUtil.storeFile(file, material);
            material.setContent(fileStorageUtil.getFileAccessUrl(newFilePath));
        }

        // 更新材料信息
        trainingMaterialMapper.updateById(material);
        return material;
    }
    @Override
    public List<TrainingMaterial> search(String keyword) {
        QueryWrapper<TrainingMaterial> query = new QueryWrapper<>();
        query.like("title", keyword)
             .or()
             .like("description", keyword);
        return trainingMaterialMapper.selectList(query);
    }
    @Override
    public String getPreviewUrl(Long materialId) {
        TrainingMaterial material = trainingMaterialMapper.selectById(materialId);
        if (material == null || material.getContent() == null || material.getContent().isEmpty()) {
            return null;
        }
        return fileStorageUtil.getPreviewUrl(material.getContent());
    }
}