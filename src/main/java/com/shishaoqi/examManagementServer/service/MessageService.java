package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     */
    boolean sendMessage(Message message);

    /**
     * 批量发送消息
     */
    boolean batchSendMessages(List<Message> messages);

    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long messageId);

    /**
     * 批量标记消息为已读
     */
    boolean markAllAsRead(Integer teacherId);

    /**
     * 获取教师未读消息
     */
    List<Message> getUnreadMessages(Integer teacherId);

    /**
     * 获取教师所有消息
     */
    List<Message> getTeacherMessages(Integer teacherId);

    /**
     * 获取教师指定类型的消息
     */
    List<Message> getMessagesByType(Integer teacherId, Integer type);

    /**
     * 获取考前培训相关消息
     */
    List<Message> getTrainingRelatedMessages(Integer teacherId, Integer type, LocalDateTime startTime);

    /**
     * 获取考试相关的紧急通知
     */
    List<Message> getUrgentExamNotifications(Integer teacherId, Integer type);

    /**
     * 获取签到提醒消息
     */
    List<Message> getSignInReminders(Integer teacherId, Integer type, LocalDateTime examDate);

    /**
     * 获取消息发送统计
     */
    List<Map<String, Object>> getMessageStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取教师的消息统计
     */
    List<Map<String, Object>> getTeacherMessageStatistics(Integer teacherId);

    /**
     * 获取每日消息统计
     */
    List<Map<String, Object>> getDailyMessageStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除过期消息
     */
    void deleteExpiredMessages(LocalDateTime expiryTime);

    /**
     * 发送考试提醒
     */
    void sendExamReminder(Integer teacherId, String examName, LocalDateTime examTime, String location);

    /**
     * 发送培训通知
     */
    void sendTrainingNotification(Integer teacherId, String trainingTitle, LocalDateTime startTime);

    /**
     * 发送紧急通知
     */
    void sendUrgentNotification(List<Integer> teacherIds, String title, String content);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(String title, String content, List<Integer> teacherIds);

    /**
     * 获取消息总数
     */
    int getMessageCount(Integer teacherId, Integer type);

    Integer getUnreadCount(Integer teacherId);

    void batchMarkAsRead(List<Long> messageIds);
}