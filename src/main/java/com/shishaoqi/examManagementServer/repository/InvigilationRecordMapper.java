package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface InvigilationRecordMapper extends BaseMapper<InvigilationRecord> {

    /**
     * 获取监考安排的所有记录
     */
    @Select("SELECT * FROM invigilation_record WHERE assignment_id = #{assignmentId} ORDER BY create_time DESC")
    List<InvigilationRecord> getRecordsByAssignment(Long assignmentId);

    /**
     * 获取监考安排的签到记录
     */
    @Select("SELECT * FROM invigilation_record WHERE assignment_id = #{assignmentId} AND type = 1")
    InvigilationRecord getSignInRecord(Long assignmentId);
}