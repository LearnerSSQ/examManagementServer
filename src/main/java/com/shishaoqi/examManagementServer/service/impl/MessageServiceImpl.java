package com.shishaoqi.examManagementServer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shishaoqi.examManagementServer.entity.Message;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.repository.MessageMapper;
import com.shishaoqi.examManagementServer.service.MessageService;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.io.Serializable;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final TeacherService teacherService;

    public MessageServiceImpl(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @Override
    public List<Message> getUnreadMessages(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取未读消息失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getStatus, 0)
                .orderByDesc(Message::getCreateTime);
        List<Message> messages = list(wrapper);
        log.info("获取教师[{}]的未读消息列表，共{}条", teacherId, messages.size());
        return messages;
    }

    @Override
    public List<Message> getTeacherMessages(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取消息列表失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .orderByDesc(Message::getCreateTime);
        List<Message> messages = list(wrapper);
        log.info("获取教师[{}]的所有消息列表，共{}条", teacherId, messages.size());
        return messages;
    }

    @Override
    public int getUnreadCount(Integer teacherId) {
        if (teacherId == null) {
            log.error("获取未读消息数量失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            log.error("获取未读消息数量失败：教师不存在，teacherId={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getStatus, 0);
        long count = count(wrapper);
        log.info("获取教师[{}]的未读消息数量：{}条", teacherId, count);
        return (int) count;
    }

    @Override
    public List<Message> getMessagesByType(Integer teacherId, Integer type) {
        if (teacherId == null || type == null) {
            log.error("获取消息列表失败：参数错误，teacherId={}, type={}", teacherId, type);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (type < 1 || type > 3) {
            log.error("获取消息列表失败：消息类型无效，type={}", type);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            log.error("获取消息列表失败：教师不存在，teacherId={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getType, type)
                .orderByDesc(Message::getCreateTime);
        List<Message> messages = list(wrapper);
        log.info("获取教师[{}]的类型[{}]消息列表，共{}条", teacherId, type, messages.size());
        return messages;
    }

    @Override
    public boolean markAsRead(Long messageId) {
        if (messageId == null) {
            log.error("标记消息已读失败：消息ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Message message = getById(messageId);
        if (message == null) {
            log.error("标记消息已读失败：消息不存在，ID={}", messageId);
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        if (message.getStatus() == 1) {
            log.warn("消息已经是已读状态，ID={}", messageId);
            throw new BusinessException(ErrorCode.MESSAGE_ALREADY_READ);
        }

        message.setStatus(1);
        message.setReadTime(LocalDateTime.now());
        boolean success = updateById(message);
        if (!success) {
            log.error("标记消息已读失败，ID={}", messageId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("成功标记消息为已读，ID={}", messageId);
        return true;
    }

    @Override
    public boolean markAllAsRead(Integer teacherId) {
        if (teacherId == null) {
            log.error("批量标记消息已读失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            log.error("批量标记消息已读失败：教师不存在，teacherId={}", teacherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getTeacherId, teacherId)
                .eq(Message::getStatus, 0);

        List<Message> unreadMessages = list(wrapper);
        if (unreadMessages.isEmpty()) {
            log.info("教师[{}]没有未读消息", teacherId);
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Message message : unreadMessages) {
            message.setStatus(1);
            message.setReadTime(now);
        }

        boolean success = updateBatchById(unreadMessages);
        if (!success) {
            log.error("批量标记消息已读失败，teacherId={}", teacherId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("成功标记教师[{}]的所有未读消息为已读，共{}条", teacherId, unreadMessages.size());
        return true;
    }

    @Override
    public boolean save(Message message) {
        if (message == null) {
            log.error("保存消息失败：消息对象为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (message.getTeacherId() == null) {
            log.error("保存消息失败：教师ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (message.getTitle() == null || message.getTitle().trim().isEmpty()) {
            log.error("保存消息失败：消息标题为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (message.getType() == null || message.getType() < 1 || message.getType() > 3) {
            log.error("保存消息失败：消息类型无效");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Teacher teacher = teacherService.getById(message.getTeacherId());
        if (teacher == null) {
            log.error("保存消息失败：教师不存在，teacherId={}", message.getTeacherId());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());

        boolean success = super.save(message);
        if (!success) {
            log.error("保存消息失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("成功保存消息，ID={}，标题：{}", message.getMessageId(), message.getTitle());
        return true;
    }

    @Override
    public boolean updateById(Message message) {
        if (message == null || message.getMessageId() == null) {
            log.error("更新消息失败：消息ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Message existingMessage = getById(message.getMessageId());
        if (existingMessage == null) {
            log.error("更新消息失败：消息不存在，ID={}", message.getMessageId());
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        Teacher teacher = teacherService.getById(message.getTeacherId());
        if (teacher == null) {
            log.error("更新消息失败：教师不存在，teacherId={}", message.getTeacherId());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        boolean success = super.updateById(message);
        if (!success) {
            log.error("更新消息失败，ID={}", message.getMessageId());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("成功更新消息，ID={}", message.getMessageId());
        return true;
    }

    @Override
    public boolean removeById(Serializable messageId) {
        if (messageId == null) {
            log.error("删除消息失败：消息ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Message message = getById(messageId);
        if (message == null) {
            log.error("删除消息失败：消息不存在，ID={}", messageId);
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        boolean success = super.removeById(messageId);
        if (!success) {
            log.error("删除消息失败，ID={}", messageId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("成功删除消息，ID={}", messageId);
        return true;
    }
}