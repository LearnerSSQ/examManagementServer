package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    /**
     * 更新学习时长
     */
    @Update("UPDATE training_record SET study_time = study_time + #{minutes} WHERE record_id = #{recordId}")
    int updateStudyTime(Long recordId, Integer minutes);

    /**
     * 更新考试成绩和完成状态
     */
    @Update("UPDATE training_record SET exam_score = #{examScore}, status = 2, complete_time = #{completeTime} WHERE record_id = #{recordId}")
    int completeExam(Long recordId, Integer examScore, LocalDateTime completeTime);

    /**
     * 更新培训记录状态
     */
    @Update("UPDATE training_record SET status = #{status} WHERE record_id = #{recordId}")
    int updateStatus(Long recordId, Integer status);
}