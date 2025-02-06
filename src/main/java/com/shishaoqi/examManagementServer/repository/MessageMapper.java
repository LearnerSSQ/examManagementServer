package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 标记消息为已读
     */
    @Update("UPDATE message SET status = 1, read_time = #{readTime} WHERE message_id = #{messageId}")
    int markAsRead(Long messageId, LocalDateTime readTime);

    /**
     * 批量标记消息为已读
     */
    @Update("UPDATE message SET status = 1, read_time = #{readTime} WHERE teacher_id = #{teacherId} AND status = 0")
    int markAllAsRead(Integer teacherId, LocalDateTime readTime);
}