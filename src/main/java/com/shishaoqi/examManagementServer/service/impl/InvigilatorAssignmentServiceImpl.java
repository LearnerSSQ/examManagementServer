package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.repository.InvigilatorAssignmentMapper;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvigilatorAssignmentServiceImpl extends ServiceImpl<InvigilatorAssignmentMapper, InvigilatorAssignment>
        implements InvigilatorAssignmentService {

    @Override
    public List<InvigilatorAssignment> getTeacherAssignments(Integer teacherId) {
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvigilatorAssignment::getTeacherId, teacherId)
                .orderByDesc(InvigilatorAssignment::getExamStart);
        return list(wrapper);
    }

    @Override
    public List<InvigilatorAssignment> getAssignmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<InvigilatorAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(InvigilatorAssignment::getExamStart, startTime, endTime)
                .orderByAsc(InvigilatorAssignment::getExamStart);
        return list(wrapper);
    }
}