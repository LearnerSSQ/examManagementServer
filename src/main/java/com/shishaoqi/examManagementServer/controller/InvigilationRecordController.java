package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.entity.InvigilationRecord;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "监考记录管理", description = "监考记录相关接口")
@RestController
@RequestMapping("/api/invigilation-records")
public class InvigilationRecordController {

    @Autowired
    private InvigilationRecordService invigilationRecordService;

    @Operation(summary = "获取监考安排的所有记录", description = "获取指定监考安排的所有相关记录", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取监考记录列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilationRecord.class)))
    })
    @GetMapping("/assignment/{assignmentId}")
    public List<InvigilationRecord> getRecordsByAssignment(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return invigilationRecordService.getRecordsByAssignment(assignmentId);
    }

    @Operation(summary = "获取签到记录", description = "获取指定监考安排的签到记录", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取签到记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilationRecord.class)))
    })
    @GetMapping("/sign-in/{assignmentId}")
    public InvigilationRecord getSignInRecord(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return invigilationRecordService.getSignInRecord(assignmentId);
    }

    @Operation(summary = "获取异常事件记录", description = "获取指定监考安排的所有异常事件记录", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取异常事件记录列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilationRecord.class)))
    })
    @GetMapping("/exceptions/{assignmentId}")
    public List<InvigilationRecord> getExceptionRecords(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return invigilationRecordService.getExceptionRecords(assignmentId);
    }

    @Operation(summary = "检查是否已签到", description = "检查指定监考安排是否已完成签到", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取签到状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/check-sign-in/{assignmentId}")
    public boolean hasSignedIn(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return invigilationRecordService.hasSignedIn(assignmentId);
    }

    @Operation(summary = "获取异常事件数量", description = "获取指定监考安排的异常事件数量", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取异常事件数量", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)))
    })
    @GetMapping("/exception-count/{assignmentId}")
    public int countExceptionRecords(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return invigilationRecordService.countExceptionRecords(assignmentId);
    }
}