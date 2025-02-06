package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import com.shishaoqi.examManagementServer.repository.InvigilationRecordMapper;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvigilationRecordServiceImpl extends ServiceImpl<InvigilationRecordMapper, InvigilationRecord>
        implements InvigilationRecordService {

    @Override
    public List<InvigilationRecord> getRecordsByAssignment(Long assignmentId) {
        return baseMapper.getRecordsByAssignment(assignmentId);
    }

    @Override
    public InvigilationRecord getSignInRecord(Long assignmentId) {
        return baseMapper.getSignInRecord(assignmentId);
    }

    @Override
    public List<InvigilationRecord> getExceptionRecords(Long assignmentId) {
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, 2) // 异常事件类型
                .orderByDesc(InvigilationRecord::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean hasSignedIn(Long assignmentId) {
        return baseMapper.getSignInRecord(assignmentId) != null;
    }

    @Override
    public int countExceptionRecords(Long assignmentId) {
        LambdaQueryWrapper<InvigilationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilationRecord::getAssignmentId, assignmentId)
                .eq(InvigilationRecord::getType, 2); // 异常事件类型
        return Math.toIntExact(count(wrapper));
    }
}