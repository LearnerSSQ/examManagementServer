package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.repository.TrainingRecordMapper;
import com.shishaoqi.examManagementServer.repository.TrainingMaterialMapper;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord>
        implements TrainingRecordService {

    private final TrainingMaterialMapper materialMapper;

    public TrainingRecordServiceImpl(TrainingMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    public List<TrainingRecord> getTeacherRecords(Integer teacherId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public List<TrainingRecord> getMaterialRecords(Long materialId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getMaterialId, materialId)
                .orderByDesc(TrainingRecord::getStartTime);
        return list(wrapper);
    }

    @Override
    public boolean hasCompletedTraining(Integer teacherId, Long materialId) {
        LambdaQueryWrapper<TrainingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrainingRecord::getTeacherId, teacherId)
                .eq(TrainingRecord::getMaterialId, materialId)
                .eq(TrainingRecord::getStatus, 2); // 已完成状态
        TrainingRecord record = getOne(wrapper);
        if (record == null) {
            return false;
        }
        // 获取培训材料的通过分数
        TrainingMaterial material = materialMapper.selectById(materialId);
        return material != null && record.getExamScore() >= material.getPassScore();
    }
}