package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InvigilationRecordService extends IService<InvigilationRecord> {

    /**
     * 获取监考安排的所有记录
     */
    List<InvigilationRecord> getRecordsByAssignment(Long assignmentId);

    /**
     * 获取监考安排的签到记录
     */
    InvigilationRecord getSignInRecord(Long assignmentId);

    /**
     * 获取监考安排的异常事件记录
     */
    List<InvigilationRecord> getExceptionRecords(Long assignmentId);

    /**
     * 检查监考安排是否已签到
     */
    boolean hasSignedIn(Long assignmentId);

    /**
     * 统计监考安排的异常事件数量
     */
    int countExceptionRecords(Long assignmentId);

    /**
     * 获取指定时间范围内的异常记录
     * 
     * @param assignmentId 监考安排ID
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 异常记录列表
     */
    List<InvigilationRecord> getExceptionRecordsByTimeRange(Long assignmentId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取最近的异常记录
     * 
     * @param assignmentId 监考安排ID
     * @param limit        限制数量
     * @return 异常记录列表
     */
    List<InvigilationRecord> getRecentExceptionRecords(Long assignmentId, int limit);

    /**
     * 获取监考记录统计信息
     * 
     * @param assignmentId 监考安排ID
     * @return 统计信息（包含总记录数、异常记录数、是否签到等）
     */
    Map<String, Object> getRecordStatistics(Long assignmentId);

    /**
     * 批量创建监考记录
     * 
     * @param records 监考记录列表
     * @return 是否全部创建成功
     */
    boolean saveBatch(List<InvigilationRecord> records);

    /**
     * 获取指定类型的监考记录
     * 
     * @param assignmentId 监考安排ID
     * @param type         记录类型
     * @return 监考记录列表
     */
    List<InvigilationRecord> getRecordsByType(Long assignmentId, Integer type);

    /**
     * 获取最近一段时间内的监考记录
     * 
     * @param assignmentId 监考安排ID
     * @param hours        最近小时数
     * @return 监考记录列表
     */
    List<InvigilationRecord> getRecentRecords(Long assignmentId, int hours);
}