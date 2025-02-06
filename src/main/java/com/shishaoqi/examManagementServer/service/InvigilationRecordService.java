package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import java.util.List;

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
}