package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.repository.MessageMapper;
import com.shishaoqi.examManagementServer.service.MessageService;
import com.shishaoqi.examManagementServer.entity.message.Message;
import com.shishaoqi.examManagementServer.entity.message.MessageType;
import com.shishaoqi.examManagementServer.entity.message.MessageStatus;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public boolean sendMessage(Message message) {
        if (message == null) {
            throw new BusinessException("消息不能为空");
        }
        message.setSendTime(LocalDateTime.now());
        message.setStatus(MessageStatus.UNREAD);
        return save(message);
    }

    @Override
    @Transactional
    public boolean batchSendMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException("消息列表不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        messages.forEach(msg -> {
            msg.setSendTime(now);
            msg.setStatus(MessageStatus.UNREAD);
        });
        return saveBatch(messages);
    }

    @Override
    @Transactional
    @CacheEvict(value = "messageCache", key = "#messageId")
    public boolean markAsRead(Long messageId) {
        return messageMapper.markAsRead(messageId, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(value = "messageCache", allEntries = true)
    public boolean markAllAsRead(Integer teacherId) {
        return messageMapper.markAllAsRead(teacherId, LocalDateTime.now()) > 0;
    }

    @Override
    @Cacheable(value = "messageCache", key = "'unread:' + #teacherId")
    public List<Message> getUnreadMessages(Integer teacherId) {
        return messageMapper.getUnreadMessages(teacherId, 10, 0);
    }

    @Override
    @Cacheable(value = "messageCache", key = "'all:' + #teacherId")
    public List<Message> getTeacherMessages(Integer teacherId) {
        return messageMapper.getTeacherMessages(teacherId, 10, 0);
    }

    @Override
    @Cacheable(value = "messageCache", key = "'type:' + #teacherId + ':' + #type")
    public List<Message> getMessagesByType(Integer teacherId, Integer type) {
        return messageMapper.getMessagesByType(teacherId, type, 10, 0);
    }

    @Override
    public List<Message> getTrainingRelatedMessages(Integer teacherId, Integer type, LocalDateTime startTime) {
        return messageMapper.getTrainingRelatedMessages(teacherId, type, startTime);
    }

    @Override
    public List<Message> getUrgentExamNotifications(Integer teacherId, Integer type) {
        return messageMapper.getUrgentExamNotifications(teacherId, type);
    }

    @Override
    public List<Message> getSignInReminders(Integer teacherId, Integer type, LocalDateTime examDate) {
        return messageMapper.getSignInReminders(teacherId, type, examDate);
    }

    @Override
    @Cacheable(value = "messageStatCache", key = "'stats:' + #startTime + ':' + #endTime")
    public List<Map<String, Object>> getMessageStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return messageMapper.getMessageStatistics(startTime, endTime);
    }

    @Override
    @Cacheable(value = "messageStatCache", key = "'teacherStats:' + #teacherId")
    public List<Map<String, Object>> getTeacherMessageStatistics(Integer teacherId) {
        return messageMapper.getTeacherMessageStatistics(teacherId);
    }

    @Override
    @Cacheable(value = "messageStatCache", key = "'daily:' + #startTime + ':' + #endTime")
    public List<Map<String, Object>> getDailyMessageStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return messageMapper.getDailyMessageStatistics(startTime, endTime);
    }

    @Override
    @Transactional
    public void deleteExpiredMessages(LocalDateTime expiryTime) {
        messageMapper.deleteExpiredMessages(expiryTime);
    }

    @Override
    @Transactional
    public void sendExamReminder(Integer teacherId, String examName, LocalDateTime examTime, String location) {
        Message message = new Message();
        message.setReceiverId(teacherId);
        message.setType(MessageType.ASSIGNMENT);
        message.setTitle("考试提醒");
        message.setContent(String.format("您有一场考试将在%s于%s举行，考试名称：%s",
                examTime.toString(), location, examName));
        sendMessage(message);
    }

    @Override
    @Transactional
    public void sendTrainingNotification(Integer teacherId, String trainingTitle, LocalDateTime startTime) {
        Message message = new Message();
        message.setReceiverId(teacherId);
        message.setType(MessageType.TRAINING);
        message.setTitle("培训通知");
        message.setContent(String.format("您有新的培训任务：%s，开始时间：%s",
                trainingTitle, startTime.toString()));
        sendMessage(message);
    }

    @Override
    @Transactional
    public void sendUrgentNotification(List<Integer> teacherIds, String title, String content) {
        List<Message> messages = new ArrayList<>();
        teacherIds.forEach(teacherId -> {
            Message message = new Message();
            message.setReceiverId(teacherId);
            message.setType(MessageType.SYSTEM);
            message.setTitle(title);
            message.setContent(content);
            messages.add(message);
        });
        batchSendMessages(messages);
    }

    @Override
    @Transactional
    public void sendSystemNotification(String title, String content, List<Integer> teacherIds) {
        List<Message> messages = new ArrayList<>();
        teacherIds.forEach(teacherId -> {
            Message message = new Message();
            message.setReceiverId(teacherId);
            message.setType(MessageType.NOTIFICATION);
            message.setTitle(title);
            message.setContent(content);
            messages.add(message);
        });
        batchSendMessages(messages);
    }

    @Override
    public int getMessageCount(Integer teacherId, Integer type) {
        return messageMapper.getMessageCount(teacherId, type);
    }

    @Override
    public Integer getUnreadCount(Integer teacherId) {
        return messageMapper.getUnreadCount(teacherId);
    }

    @Override
    @Transactional
    public void batchMarkAsRead(List<Long> messageIds) {
        messageMapper.batchUpdateStatus(messageIds, MessageStatus.READ.getValue(), LocalDateTime.now());
    }
}