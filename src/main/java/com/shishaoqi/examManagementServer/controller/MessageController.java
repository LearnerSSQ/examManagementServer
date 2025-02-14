package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.message.Message;
import com.shishaoqi.examManagementServer.entity.message.MessageStatus;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息通知管理", description = "处理系统消息通知相关的接口")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

        private static final Logger log = LoggerFactory.getLogger(MessageController.class);

        @Autowired
        private MessageService messageService;

        @Operation(summary = "获取未读消息列表", description = "获取指定教师的所有未读消息列表")
        @ApiResponse(responseCode = "200", description = "成功获取未读消息列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Message.class))))
        @GetMapping("/unread/{teacherId}")
        public Result<List<Message>> getUnreadMessages(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                try {
                        log.info("获取教师{}的未读消息列表", teacherId);
                        List<Message> messages = messageService.getUnreadMessages(teacherId);
                        return Result.success(messages);
                } catch (Exception e) {
                        log.error("获取未读消息列表失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "获取未读消息失败");
                }
        }

        @Operation(summary = "获取所有消息列表", description = "获取指定教师的所有消息列表")
        @ApiResponse(responseCode = "200", description = "成功获取消息列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Message.class))))
        @GetMapping("/teacher/{teacherId}")
        public Result<List<Message>> getTeacherMessages(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                try {
                        log.info("获取教师{}的所有消息列表", teacherId);
                        List<Message> messages = messageService.getTeacherMessages(teacherId);
                        return Result.success(messages);
                } catch (Exception e) {
                        log.error("获取消息列表失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "获取消息失败");
                }
        }

        @Operation(summary = "获取未读消息数量", description = "获取指定教师的未读消息数量", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取未读消息数量", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)))
        })
        @GetMapping("/unread-count/{teacherId}")
        public Result<Integer> getUnreadCount(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                return Result.success(messageService.getUnreadCount(teacherId));
        }

        @Operation(summary = "获取指定类型的消息列表", description = "获取指定教师的指定类型的消息列表", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取消息列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
        })
        @GetMapping("/type")
        public Result<List<Message>> getMessagesByType(
                        @Parameter(description = "教师ID", required = true) @RequestParam Integer teacherId,
                        @Parameter(description = "消息类型（1=系统通知, 2=监考提醒, 3=培训通知）", required = true) @RequestParam Integer type) {
                return Result.success(messageService.getMessagesByType(teacherId, type));
        }

        @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
        @ApiResponse(responseCode = "200", description = "成功标记消息为已读")
        @PutMapping("/{messageId}/read")
        @Transactional
        public Result<Void> markAsRead(
                        @Parameter(description = "消息ID", required = true) @PathVariable Long messageId) {
                try {
                        log.info("标记消息{}为已读", messageId);
                        boolean success = messageService.markAsRead(messageId);
                        if (!success) {
                                return Result.error(ErrorCode.SYSTEM_ERROR, "标记消息为已读失败");
                        }
                        return Result.success(null);
                } catch (Exception e) {
                        log.error("标记消息为已读失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "标记消息为已读失败");
                }
        }

        @Operation(summary = "批量标记消息为已读", description = "将指定教师的所有未读消息标记为已读状态")
        @ApiResponse(responseCode = "200", description = "成功批量标记消息为已读")
        @PutMapping("/teacher/{teacherId}/read-all")
        @Transactional
        public Result<Void> markAllAsRead(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                try {
                        log.info("批量标记教师{}的所有未读消息为已读", teacherId);
                        boolean success = messageService.markAllAsRead(teacherId);
                        if (!success) {
                                return Result.error(ErrorCode.SYSTEM_ERROR, "批量标记消息为已读失败");
                        }
                        return Result.success(null);
                } catch (Exception e) {
                        log.error("批量标记消息为已读失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "批量标记消息为已读失败");
                }
        }

        @Operation(summary = "删除消息", description = "删除指定的消息")
        @ApiResponse(responseCode = "200", description = "成功删除消息")
        @DeleteMapping("/{messageId}")
        @Transactional
        public Result<Void> deleteMessage(
                        @Parameter(description = "消息ID", required = true) @PathVariable Long messageId) {
                try {
                        log.info("删除消息{}", messageId);
                        boolean success = messageService.removeById(messageId);
                        if (!success) {
                                return Result.error(ErrorCode.SYSTEM_ERROR, "删除消息失败");
                        }
                        return Result.success(null);
                } catch (Exception e) {
                        log.error("删除消息失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "删除消息失败");
                }
        }

        @Operation(summary = "发送消息", description = "发送新的消息")
        @ApiResponse(responseCode = "200", description = "成功发送消息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
        @PostMapping
        @Transactional
        public Result<Message> sendMessage(@RequestBody Message message) {
                try {
                        log.info("发送新消息：{}", message);
                        message.setStatus(MessageStatus.UNREAD);
                        boolean success = messageService.sendMessage(message);
                        if (!success) {
                                return Result.error(ErrorCode.SYSTEM_ERROR, "发送消息失败");
                        }
                        return Result.success(message);
                } catch (Exception e) {
                        log.error("发送消息失败", e);
                        return Result.error(ErrorCode.SYSTEM_ERROR, "发送消息失败");
                }
        }
}