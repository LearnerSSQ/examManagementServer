package com.shishaoqi.examManagementServer.service;

import com.shishaoqi.examManagementServer.entity.Message;
import com.shishaoqi.examManagementServer.repository.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @MockBean
    private MessageMapper messageMapper;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setMessageId(1L);
        testMessage.setTeacherId(1);
        testMessage.setTitle("监考通知");
        testMessage.setContent("您有新的监考任务");
        testMessage.setType(2);
        testMessage.setStatus(0);
        testMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getUnreadMessages() {
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.selectList(any())).thenReturn(messages);

        List<Message> result = messageService.getUnreadMessages(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getStatus());
        assertEquals(2, result.get(0).getType());
    }

    @Test
    void getTeacherMessages() {
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.selectList(any())).thenReturn(messages);

        List<Message> result = messageService.getTeacherMessages(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTeacherId());
        assertEquals("监考通知", result.get(0).getTitle());
    }

    @Test
    void getMessagesByType() {
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.selectList(any())).thenReturn(messages);

        List<Message> result = messageService.getMessagesByType(1, 2);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getType());
        assertEquals("您有新的监考任务", result.get(0).getContent());
    }
}