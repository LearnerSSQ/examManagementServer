package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.Teacher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InvigilatorAssignmentService extends IService<InvigilatorAssignment> {

    /**
     * 智能分配监考教师
     * 
     * @param examId        考试ID
     * @param requiredCount 所需监考人数
     * @return 分配结果
     */
    List<Map<String, Object>> autoAssignInvigilators(Long examId, int requiredCount);

    /**
     * 获取教师的监考安排
     * 
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 监考安排列表
     */
    List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * 批量确认监考安排
     * 
     * @param assignmentIds 安排ID列表
     * @return 确认结果
     */
    boolean batchConfirmAssignments(List<Long> assignmentIds);

    /**
     * 检查并解决时间冲突
     * 
     * @param assignment 新的监考安排
     * @return 冲突解决方案
     */
    Map<String, Object> resolveTimeConflict(InvigilatorAssignment assignment);

    /**
     * 获取考试场次的监考安排统计
     * 
     * @param examId 考试ID
     * @return 统计信息
     */
    Map<String, Object> getExamAssignmentStatistics(Long examId);

    /**
     * 根据专业匹配监考教师
     * 
     * @param subjectArea 专业领域
     * @param count       所需人数
     * @return 匹配的教师列表
     */
    List<Teacher> matchInvigilatorsBySpecialty(String subjectArea, int count);

    /**
     * 获取教师监考工作量平衡报告
     * 
     * @param departmentId 部门ID
     * @return 工作量分布报告
     */
    Map<String, Object> getWorkloadBalanceReport(String departmentId);

    /**
     * 生成监考安排建议
     * 
     * @param examId 考试ID
     * @return 建议列表
     */
    List<Map<String, Object>> generateAssignmentSuggestions(Long examId);

    /**
     * 批量调整监考安排
     * 
     * @param adjustments 调整方案列表
     * @return 调整结果
     */
    boolean batchAdjustAssignments(List<Map<String, Object>> adjustments);

    /**
     * 获取监考安排冲突报告
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 冲突报告
     */
    List<Map<String, Object>> getConflictReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取教师的监考安排列表
     */
    List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId);

    /**
     * 获取时间段内的监考安排
     */
    List<InvigilatorAssignment> getAssignmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新监考安排状态
     */
    boolean updateStatus(Long assignmentId, Integer status);

    /**
     * 取消监考安排
     */
    boolean cancelAssignment(Long assignmentId);
}