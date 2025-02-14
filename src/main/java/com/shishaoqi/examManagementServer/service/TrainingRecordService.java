package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;

import java.time.LocalDateTime;
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
    boolean updateScore(Long recordId, Integer score, TrainingRecordStatus status);

    /**
     * 更新学习进度
     */
    boolean updateProgress(Long recordId, Integer teacherId, Integer progress);

    /**
     * 验证是否达到规定学习时长
     */
    boolean validateStudyTime(Long recordId);

    /**
     * 获取培训统计信息
     */
    Map<String, Object> getTrainingStatistics(Integer teacherId);

    /**
     * 获取未完成的培训列表
     */
    List<TrainingRecord> getUnfinishedTrainings(Integer teacherId);

    /**
     * 获取最近的培训记录
     */
    List<TrainingRecord> getRecentTrainings(Integer teacherId, int months);

    /**
     * 检查培训是否过期
     */
    boolean isTrainingExpired(Integer teacherId, Long materialId);

    /**
     * 获取教师的培训完成率
     */
    double getCompletionRate(Integer teacherId);

    /**
     * 获取教师的平均培训成绩
     */
    double getAverageScore(Integer teacherId);

    /**
     * 批量检查培训状态
     */
    Map<Integer, Boolean> batchCheckTrainingStatus(List<Integer> teacherIds, Long materialId);

    /**
     * 获取教师的必修培训完成情况
     */
    Map<String, Object> getRequiredTrainingStatus(Integer teacherId);

    /**
     * 获取教师的培训合格证明
     * 
     * @param teacherId  教师ID
     * @param materialId 培训材料ID
     * @return 包含证书信息的Map
     */
    Map<String, Object> getTrainingCertificate(Integer teacherId, Long materialId);

    /**
     * 批量分配培训任务
     * 
     * @param teacherIds 教师ID列表
     * @param materialId 培训材料ID
     * @param deadline   截止时间
     * @return 分配结果
     */
    boolean assignTrainingBatch(List<Integer> teacherIds, Long materialId, LocalDateTime deadline);

    /**
     * 获取即将过期的培训记录
     * 
     * @param teacherId     教师ID
     * @param daysThreshold 天数阈值
     * @return 即将过期的培训记录列表
     */
    List<TrainingRecord> getExpiringTrainings(Integer teacherId, int daysThreshold);

    /**
     * 重置培训进度
     * 
     * @param recordId 记录ID
     * @return 是否重置成功
     */
    boolean resetTrainingProgress(Long recordId);

    /**
     * 根据时间范围获取培训记录
     */
    List<TrainingRecord> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围获取教师的培训记录
     */
    List<TrainingRecord> getTeacherRecordsByTimeRange(Integer teacherId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取培训统计信息
     */
    Map<String, Object> getTrainingStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取教师的培训记录列表
     * 
     * @param teacherId 教师ID
     * @param status    状态筛选（可选）
     * @return 培训记录列表
     */
    List<TrainingRecord> getTeacherTrainings(Integer teacherId, TrainingRecordStatus status);

    /**
     * 开始培训
     * 
     * @param recordId  培训记录ID
     * @param teacherId 教师ID
     */
    void startTraining(Long recordId, Integer teacherId);

    /**
     * 获取培训记录详情
     * 
     * @param recordId  培训记录ID
     * @param teacherId 教师ID
     * @return 培训记录详情
     */
    TrainingRecord getTrainingRecord(Long recordId, Integer teacherId);

    /**
     * 获取指定时间范围内的培训统计数据
     * 
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 培训统计数据，包含：
     *         - activeCount: 进行中的培训数量
     *         - completedCount: 已完成的培训数量
     *         - totalCount: 总培训数量
     *         - completionRate: 完成率
     *         - inTrainingCount: 培训中的教师数量
     */
    Map<String, Object> getTrainingStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取教师的培训统计数据
     * 
     * @param teacherId 教师ID
     * @return 培训统计数据，包含：
     *         - pendingCount: 待完成的培训数量
     *         - completedCount: 已完成的培训数量
     *         - totalCount: 总培训数量
     *         - completionRate: 完成率
     */
    Map<String, Object> getTeacherTrainingStatistics(Integer teacherId);
}