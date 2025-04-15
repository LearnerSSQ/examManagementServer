package com.shishaoqi.examManagementServer.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shishaoqi.examManagementServer.entity.message.Message;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

        /**
         * 标记消息为已读
         */
        @Update("UPDATE message SET status = 'READ', read_time = #{readTime} WHERE message_id = #{messageId}")
        int markAsRead(@Param("messageId") Long messageId, @Param("readTime") LocalDateTime readTime);

        /**
         * 批量标记消息为已读
         */
        @Update("UPDATE message SET status = 'READ', read_time = #{readTime} " +
                        "WHERE receiver_id = #{teacherId} AND status = 'UNREAD'")
        int markAllAsRead(@Param("teacherId") Integer teacherId, @Param("readTime") LocalDateTime readTime);

        /**
         * 获取教师未读消息数量
         */
        @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{teacherId} AND status = 'UNREAD'")
        int getUnreadCount(@Param("teacherId") Integer teacherId);

        /**
         * 获取教师指定类型的消息
         */
        @Select("SELECT * FROM message WHERE receiver_id = #{teacherId} AND type = #{type} " +
                        "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
        List<Message> getMessagesByType(@Param("teacherId") Integer teacherId,
                        @Param("type") Integer type,
                        @Param("limit") Integer limit,
                        @Param("offset") Integer offset);

        /**
         * 获取考前培训相关的消息
         */
        @Select("SELECT * FROM message WHERE receiver_id = #{teacherId} " +
                        "AND type = #{type} AND category = 'TRAINING' " +
                        "AND create_time >= #{startTime} " +
                        "ORDER BY priority DESC, create_time DESC")
        List<Message> getTrainingRelatedMessages(@Param("teacherId") Integer teacherId,
                        @Param("type") Integer type,
                        @Param("startTime") LocalDateTime startTime);

        /**
         * 获取考试相关的紧急通知
         */
        @Select("SELECT * FROM message WHERE receiver_id = #{teacherId} " +
                        "AND type = #{type} AND priority = 'HIGH' " +
                        "AND category = 'EXAM' AND status = 0 " +
                        "ORDER BY create_time DESC")
        List<Message> getUrgentExamNotifications(@Param("teacherId") Integer teacherId,
                        @Param("type") Integer type);

        /**
         * 获取签到提醒消息
         */
        @Select("SELECT * FROM message WHERE receiver_id = #{teacherId} " +
                        "AND type = #{type} AND category = 'SIGN_IN' " +
                        "AND exam_date = #{examDate} " +
                        "ORDER BY create_time DESC")
        List<Message> getSignInReminders(@Param("teacherId") Integer teacherId,
                        @Param("type") Integer type,
                        @Param("examDate") LocalDateTime examDate);

        /**
         * 获取消息发送统计（带缓存）
         */
        @Options(useCache = true)
        @Select("SELECT type, COUNT(*) as count, " +
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as read_count, " +
                        "MAX(create_time) as last_message_time " +
                        "FROM message " +
                        "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY type")
        List<Map<String, Object>> getMessageStatistics(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取教师的消息统计（带缓存）
         */
        @Options(useCache = true)
        @Select("SELECT type, COUNT(*) as count, " +
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as read_count, " +
                        "MAX(create_time) as last_message_time " +
                        "FROM message " +
                        "WHERE receiver_id = #{teacherId} " +
                        "GROUP BY type")
        List<Map<String, Object>> getTeacherMessageStatistics(@Param("teacherId") Integer teacherId);

        /**
         * 获取指定时间范围内的消息
         */
        @Select("SELECT * FROM message " +
                        "WHERE send_time BETWEEN #{startTime} AND #{endTime} " +
                        "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
        List<Message> getMessagesByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("limit") Integer limit,
                        @Param("offset") Integer offset);

        /**
         * 获取教师的紧急通知
         */
        @Select("SELECT * FROM message " +
                        "WHERE receiver_id = #{teacherId} " +
                        "AND type = 1 " +
                        "AND title = '紧急通知' " +
                        "ORDER BY send_time DESC LIMIT #{limit}")
        List<Message> getUrgentNotifications(@Param("teacherId") Integer teacherId,
                        @Param("limit") Integer limit);

        /**
         * 获取教师的监考提醒
         */
        @Select("SELECT * FROM message " +
                        "WHERE receiver_id = #{teacherId} " +
                        "AND type = 2 " +
                        "ORDER BY send_time DESC LIMIT #{limit}")
        List<Message> getExamReminders(@Param("teacherId") Integer teacherId,
                        @Param("limit") Integer limit);

        /**
         * 获取教师的培训通知
         */
        @Select("SELECT * FROM message " +
                        "WHERE receiver_id = #{teacherId} " +
                        "AND type = 3 " +
                        "ORDER BY send_time DESC LIMIT #{limit}")
        List<Message> getTrainingNotifications(@Param("teacherId") Integer teacherId,
                        @Param("limit") Integer limit);

        /**
         * 删除过期消息
         */
        @Update("DELETE FROM message WHERE create_time < #{expiryTime}")
        int deleteExpiredMessages(@Param("expiryTime") LocalDateTime expiryTime);

        /**
         * 获取教师的所有未读消息
         */
        @Select("SELECT * FROM message " +
                        "WHERE receiver_id = #{teacherId} AND status = 0 " +
                        "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
        List<Message> getUnreadMessages(@Param("teacherId") Integer teacherId,
                        @Param("limit") Integer limit,
                        @Param("offset") Integer offset);

        /**
         * 获取教师的所有消息
         */
        @Select("SELECT * FROM message " +
                        "WHERE receiver_id = #{teacherId} " +
                        "ORDER BY send_time DESC LIMIT #{limit} OFFSET #{offset}")
        List<Message> getTeacherMessages(@Param("teacherId") Integer teacherId,
                        @Param("limit") Integer limit,
                        @Param("offset") Integer offset);

        /**
         * 批量插入消息
         */
        @Insert("<script>" +
                        "INSERT INTO message (receiver_id, type, title, content, status, send_time, reference_id, require_confirm) VALUES "
                        +
                        "<foreach collection='messages' item='msg' separator=','>" +
                        "(#{msg.receiverId}, #{msg.type}, #{msg.title}, #{msg.content}, #{msg.status}, " +
                        "#{msg.sendTime}, #{msg.referenceId}, #{msg.requireConfirm})" +
                        "</foreach>" +
                        "</script>")
        int batchInsertMessages(@Param("messages") List<Message> messages);

        /**
         * 获取指定时间范围内的消息统计（带缓存）
         */
        @Options(useCache = true)
        @Select("SELECT DATE(create_time) as date, " +
                        "COUNT(*) as total_count, " +
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as read_count, " +
                        "type, category, " +
                        "MAX(create_time) as last_message_time " +
                        "FROM message " +
                        "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
                        "GROUP BY DATE(create_time), type, category")
        List<Map<String, Object>> getDailyMessageStatistics(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取消息总数
         */
        @Select("SELECT COUNT(*) FROM message " +
                        "WHERE receiver_id = #{teacherId} AND type = #{type}")
        int getMessageCount(@Param("teacherId") Integer teacherId, @Param("type") Integer type);

        /**
         * 批量更新消息状态
         */
        @Update("<script>" +
                        "UPDATE message SET status = #{status}, read_time = #{readTime} " +
                        "WHERE message_id IN " +
                        "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "</script>")
        int batchUpdateStatus(@Param("messageIds") List<Long> messageIds,
                        @Param("status") String status,
                        @Param("readTime") LocalDateTime readTime);
}