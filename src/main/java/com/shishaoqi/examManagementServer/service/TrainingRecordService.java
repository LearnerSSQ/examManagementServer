package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import java.util.List;

public interface TrainingRecordService extends IService<TrainingRecord> {

    /**
     * 获取教师的培训记录列表
     */
    List<TrainingRecord> getTeacherRecords(Integer teacherId);

    /**
     * 获取培训材料的学习记录列表
     */
    List<TrainingRecord> getMaterialRecords(Long materialId);

    /**
     * 检查教师是否完成指定培训
     */
    boolean hasCompletedTraining(Integer teacherId, Long materialId);
}