package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.entity.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "监考安排管理", description = "监考安排相关接口")
@RestController
@RequestMapping("/api/assignments")
public class InvigilatorAssignmentController {

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Operation(summary = "获取教师的监考安排", description = "获取指定教师的所有监考安排列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取监考安排列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    })
    @GetMapping("/teacher/{teacherId}")
    public List<InvigilatorAssignment> getTeacherAssignments(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
        return assignmentService.getTeacherAssignments(teacherId);
    }

    @Operation(summary = "获取时间段内的监考安排", description = "获取指定时间范围内的所有监考安排", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取监考安排列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    })
    @GetMapping("/timeRange")
    public List<InvigilatorAssignment> getAssignmentsByTimeRange(
            @Parameter(description = "开始时间", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return assignmentService.getAssignmentsByTimeRange(startTime, endTime);
    }

    @Operation(summary = "创建监考安排", description = "创建新的监考安排", responses = {
            @ApiResponse(responseCode = "200", description = "成功创建监考安排", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    })
    @PostMapping
    public InvigilatorAssignment createAssignment(@RequestBody InvigilatorAssignment assignment) {
        boolean success = assignmentService.saveOrUpdate(assignment);
        return success ? assignment : null;
    }

    @Operation(summary = "更新监考安排", description = "更新指定ID的监考安排", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新监考安排", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    })
    @PutMapping("/{assignmentId}")
    public InvigilatorAssignment updateAssignment(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId,
            @RequestBody InvigilatorAssignment assignment) {
        assignment.setAssignmentId(assignmentId);
        return assignmentService.updateById(assignment) ? assignment : null;
    }

    @Operation(summary = "更新监考安排状态", description = "更新指定监考安排的状态（确认/取消等）", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @PutMapping("/{assignmentId}/status/{status}")
    public boolean updateStatus(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId,
            @Parameter(description = "状态（0=未确认, 1=已确认, 2=已取消）", required = true) @PathVariable Integer status) {
        return assignmentService.updateStatus(assignmentId, status);
    }

    @Operation(summary = "取消监考安排", description = "取消指定ID的监考安排", responses = {
            @ApiResponse(responseCode = "200", description = "成功取消监考安排", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @PutMapping("/{assignmentId}/cancel")
    public boolean cancelAssignment(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return assignmentService.cancelAssignment(assignmentId);
    }

    @Operation(summary = "删除监考安排", description = "删除指定ID的监考安排", responses = {
            @ApiResponse(responseCode = "200", description = "成功删除监考安排", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @DeleteMapping("/{assignmentId}")
    public boolean deleteAssignment(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return assignmentService.removeById(assignmentId);
    }
}