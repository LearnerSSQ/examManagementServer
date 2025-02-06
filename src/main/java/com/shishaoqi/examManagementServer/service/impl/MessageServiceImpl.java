package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Message;
import com.shishaoqi.examManagementServer.repository.MessageMapper;
import com.shishaoqi.examManagementServer.service.MessageService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public List<Message> getUnreadMessages(Integer teacherId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getStatus, 0)
                .orderByDesc(Message::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Message> getTeacherMessages(Integer teacherId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .orderByDesc(Message::getCreateTime);
        return list(wrapper);
    }

    @Override
    public int getUnreadCount(Integer teacherId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getStatus, 0);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public List<Message> getMessagesByType(Integer teacherId, Integer type) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getType, type)
                .orderByDesc(Message::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean markAsRead(Long messageId) {
        return baseMapper.markAsRead(messageId, LocalDateTime.now()) > 0;
    }

    @Override
    public boolean markAllAsRead(Integer teacherId) {
        return baseMapper.markAllAsRead(teacherId, LocalDateTime.now()) > 0;
    }
}