package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    /**
     * 更新学习时长
     */
    @Update("UPDATE training_record SET study_time = study_time + #{minutes} WHERE record_id = #{recordId}")
    int updateStudyTime(@Param("recordId") Long recordId, @Param("minutes") Integer minutes);

    /**
     * 更新考试成绩和完成状态
     */
    @Update("UPDATE training_record SET exam_score = #{examScore}, status = 2, complete_time = #{completeTime} WHERE record_id = #{recordId}")
    int completeExam(@Param("recordId") Long recordId, @Param("examScore") Integer examScore,
            @Param("completeTime") LocalDateTime completeTime);

    /**
     * 更新培训记录状态
     */
    @Update("UPDATE training_record SET status = #{status} WHERE record_id = #{recordId}")
    int updateStatus(@Param("recordId") Long recordId, @Param("status") Integer status);

    @Update("UPDATE training_record SET score = #{score}, status = #{status}, complete_time = #{completeTime} WHERE record_id = #{recordId}")
    int updateScore(@Param("recordId") Long recordId, @Param("score") Integer score, @Param("status") Integer status,
            @Param("completeTime") LocalDateTime completeTime);
}