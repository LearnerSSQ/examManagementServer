package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import java.util.List;
import java.util.Map;

public interface TrainingRecordService extends IService<TrainingRecord> {

    /**
     * 创建培训记录
     */
    TrainingRecord createRecord(Integer teacherId, Long materialId);

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

    /**
     * 更新培训成绩和状态
     */
    boolean updateScore(Long recordId, Integer score, Integer status);

    /**
     * 更新学习进度
     * 
     * @param recordId  记录ID
     * @param studyTime 学习时长（分钟）
     * @return 是否更新成功
     */
    boolean updateStudyProgress(Long recordId, Integer studyTime);

    /**
     * 验证是否达到规定学习时长
     * 
     * @param recordId 记录ID
     * @return 是否达到要求
     */
    boolean validateStudyTime(Long recordId);

    /**
     * 获取培训统计信息
     * 
     * @param teacherId 教师ID
     * @return 统计信息（包含完成率、平均分等）
     */
    Map<String, Object> getTrainingStatistics(Integer teacherId);

    /**
     * 获取未完成的培训列表
     * 
     * @param teacherId 教师ID
     * @return 未完成的培训记录列表
     */
    List<TrainingRecord> getUnfinishedTrainings(Integer teacherId);

    /**
     * 获取最近的培训记录
     * 
     * @param teacherId 教师ID
     * @param months    最近几个月
     * @return 培训记录列表
     */
    List<TrainingRecord> getRecentTrainings(Integer teacherId, int months);

    /**
     * 检查培训是否过期
     * 
     * @param teacherId  教师ID
     * @param materialId 培训材料ID
     * @return 是否过期
     */
    boolean isTrainingExpired(Integer teacherId, Long materialId);

    /**
     * 获取教师的培训完成率
     * 
     * @param teacherId 教师ID
     * @return 完成率（0-100）
     */
    double getCompletionRate(Integer teacherId);

    /**
     * 获取教师的平均培训成绩
     * 
     * @param teacherId 教师ID
     * @return 平均成绩
     */
    double getAverageScore(Integer teacherId);

    /**
     * 批量检查培训状态
     * 
     * @param teacherIds 教师ID列表
     * @param materialId 培训材料ID
     * @return 教师ID -> 培训状态的映射
     */
    Map<Integer, Boolean> batchCheckTrainingStatus(List<Integer> teacherIds, Long materialId);
}