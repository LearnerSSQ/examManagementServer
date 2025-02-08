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

    /**
     * 记录监考签到
     * 
     * @param assignmentId 监考安排ID
     * @param signInTime   签到时间
     * @return 签到记录
     */
    InvigilationRecord recordSignIn(Long assignmentId, LocalDateTime signInTime);

    /**
     * 记录考试异常事件
     * 
     * @param assignmentId 监考安排ID
     * @param eventType    事件类型
     * @param description  事件描述
     * @return 记录结果
     */
    InvigilationRecord recordExceptionEvent(Long assignmentId, Integer eventType, String description);

    /**
     * 获取考试异常事件统计
     * 
     * @param examId 考试ID
     * @return 统计信息
     */
    Map<String, Object> getExceptionStatistics(Long examId);

    /**
     * 获取监考记录详情
     * 
     * @param assignmentId 监考安排ID
     * @return 记录列表
     */
    List<InvigilationRecord> getAssignmentRecords(Long assignmentId);

    /**
     * 更新事件处理状态
     * 
     * @param recordId       记录ID
     * @param status         处理状态
     * @param handlerComment 处理意见
     * @return 更新结果
     */
    boolean updateEventStatus(Long recordId, Integer status, String handlerComment);

    /**
     * 生成监考报告
     * 
     * @param assignmentId 监考安排ID
     * @return 报告内容
     */
    Map<String, Object> generateInvigilationReport(Long assignmentId);

    /**
     * 批量导出监考记录
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 记录列表
     */
    List<Map<String, Object>> exportInvigilationRecords(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取教师的监考记录统计
     * 
     * @param teacherId 教师ID
     * @param year      年份
     * @return 统计信息
     */
    Map<String, Object> getTeacherInvigilationStats(Integer teacherId, int year);

    /**
     * 记录考场巡查结果
     * 
     * @param assignmentId      监考安排ID
     * @param inspectionDetails 巡查详情
     * @return 记录结果
     */
    boolean recordInspectionResult(Long assignmentId, Map<String, Object> inspectionDetails);

    /**
     * 获取未处理的异常事件
     * 
     * @return 未处理的事件列表
     */
    List<InvigilationRecord> getPendingExceptionEvents();
}