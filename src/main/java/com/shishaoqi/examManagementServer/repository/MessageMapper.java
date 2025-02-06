package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 标记消息为已读
     */
    @Update("UPDATE message SET status = 1, read_time = #{readTime} WHERE message_id = #{messageId}")
    int markAsRead(@Param("messageId") Long messageId, @Param("readTime") LocalDateTime readTime);

    /**
     * 批量标记消息为已读
     */
    @Update("UPDATE message SET status = 1, read_time = #{readTime} WHERE teacher_id = #{teacherId} AND status = 0")
    int markAllAsRead(@Param("teacherId") Integer teacherId, @Param("readTime") LocalDateTime readTime);

    /**
     * 查询消息状态
     */
    @Select("SELECT status FROM message WHERE message_id = #{messageId}")
    Integer getMessageStatus(@Param("messageId") Long messageId);
}