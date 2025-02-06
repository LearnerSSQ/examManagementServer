package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.Message;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息通知管理", description = "消息通知相关接口")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

        @Autowired
        private MessageService messageService;

        @Operation(summary = "获取未读消息列表", description = "获取指定教师的所有未读消息列表", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取未读消息列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
        })
        @GetMapping("/unread/{teacherId}")
        public Result<List<Message>> getUnreadMessages(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                return Result.success(messageService.getUnreadMessages(teacherId));
        }

        @Operation(summary = "获取所有消息列表", description = "获取指定教师的所有消息列表", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取消息列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
        })
        @GetMapping("/teacher/{teacherId}")
        public Result<List<Message>> getTeacherMessages(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                return Result.success(messageService.getTeacherMessages(teacherId));
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

        @Operation(summary = "创建消息", description = "创建新的消息通知", responses = {
                        @ApiResponse(responseCode = "200", description = "成功创建消息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
        })
        @PostMapping
        public Result<Message> createMessage(@RequestBody Message message) {
                boolean success = messageService.save(message);
                return success ? Result.success(message) : Result.error(ErrorCode.SYSTEM_ERROR);
        }

        @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态", responses = {
                        @ApiResponse(responseCode = "200", description = "成功标记消息为已读", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/{messageId}/read")
        @Transactional
        public Result<Boolean> markAsRead(
                        @Parameter(description = "消息ID", required = true) @PathVariable Long messageId) {
                boolean success = messageService.markAsRead(messageId);
                return Result.success(success);
        }

        @Operation(summary = "标记所有消息为已读", description = "将指定教师的所有未读消息标记为已读状态", responses = {
                        @ApiResponse(responseCode = "200", description = "成功标记所有消息为已读", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/teacher/{teacherId}/read-all")
        public Result<Boolean> markAllAsRead(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                boolean success = messageService.markAllAsRead(teacherId);
                return Result.success(success);
        }

        @Operation(summary = "删除消息", description = "删除指定ID的消息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功删除消息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @DeleteMapping("/{messageId}")
        public Result<Boolean> deleteMessage(
                        @Parameter(description = "消息ID", required = true) @PathVariable Long messageId) {
                boolean success = messageService.removeById(messageId);
                return Result.success(success);
        }
}