package com.shishaoqi.examManagementServer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shishaoqi.examManagementServer.entity.Message;
import java.util.List;

public interface MessageService extends IService<Message> {

    /**
     * 获取教师的未读消息列表
     */
    List<Message> getUnreadMessages(Integer teacherId);

    /**
     * 获取教师的所有消息列表
     */
    List<Message> getTeacherMessages(Integer teacherId);

    /**
     * 获取教师的未读消息数量
     */
    int getUnreadCount(Integer teacherId);

    /**
     * 根据类型获取消息列表
     */
    List<Message> getMessagesByType(Integer teacherId, Integer type);
}