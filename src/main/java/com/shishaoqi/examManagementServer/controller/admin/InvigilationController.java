package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilationRecord;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

@Tag(name = "监考管理", description = "处理监考分配和记录相关的接口")
@RestController
@RequestMapping("/api/admin/invigilation")
@Validated
public class InvigilationController {

    @Autowired
    private InvigilationRecordService invigilationRecordService;

    @Autowired
    private InvigilatorAssignmentService invigilatorAssignmentService;

    @Operation(summary = "创建监考分配", description = "创建新的监考任务分配")
    @ApiResponse(responseCode = "200", description = "成功创建监考分配", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilatorAssignment.class)))
    @PostMapping("/assignments")
    public Result<InvigilatorAssignment> createAssignment(@RequestBody InvigilatorAssignment assignment) {
        invigilatorAssignmentService.save(assignment);
        return Result.success(assignment);
    }

    @Operation(summary = "获取监考分配列表", description = "获取指定时间范围内的监考分配列表")
    @ApiResponse(responseCode = "200", description = "成功获取监考分配列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilatorAssignment.class))))
    @GetMapping("/assignments")
    public Result<List<InvigilatorAssignment>> listAssignments(
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(invigilatorAssignmentService.getByTimeRange(startTime, endTime));
        }
        return Result.success(invigilatorAssignmentService.list());
    }

    @Operation(summary = "获取教师的监考分配", description = "获取指定教师在给定时间范围内的监考分配")
    @ApiResponse(responseCode = "200", description = "成功获取教师的监考分配", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilatorAssignment.class))))
    @GetMapping("/assignments/teacher/{teacherId}")
    public Result<List<InvigilatorAssignment>> getTeacherAssignments(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(
                    invigilatorAssignmentService.getTeacherAssignmentsByTimeRange(teacherId, startTime, endTime));
        }
        return Result.success(invigilatorAssignmentService.getTeacherAssignments(teacherId));
    }

    @Operation(summary = "获取所有监考安排", description = "管理员获取所有监考安排信息")
    @GetMapping("/assignments/all")
    @PreAuthorize("hasRole('EXAM_ADMIN')")
    public Result<List<InvigilatorAssignment>> getAllAssignments(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<InvigilatorAssignment> assignments;
            if (startTime != null && endTime != null) {
                assignments = invigilatorAssignmentService.getByTimeRange(startTime, endTime);
            } else {
                assignments = invigilatorAssignmentService.list();
            }
            if (assignments == null) {
                return Result.error(500, "获取监考安排失败：数据为空");
            }
            return Result.success(assignments);
        } catch (Exception e) {
            e.printStackTrace(); // 添加日志输出
            return Result.error(500, "获取监考安排失败：" + e.getMessage());
        }
    }

    @Operation(summary = "创建监考记录", description = "创建新的监考记录")
    @ApiResponse(responseCode = "200", description = "成功创建监考记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvigilationRecord.class)))
    @PostMapping("/records")
    public Result<InvigilationRecord> createRecord(@RequestBody InvigilationRecord record) {
        invigilationRecordService.save(record);
        return Result.success(record);
    }

    @Operation(summary = "获取监考记录列表", description = "获取指定时间范围内的监考记录列表")
    @ApiResponse(responseCode = "200", description = "成功获取监考记录列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilationRecord.class))))
    @GetMapping("/records")
    public Result<List<InvigilationRecord>> listRecords(
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(invigilationRecordService.getByTimeRange(startTime, endTime));
        }
        return Result.success(invigilationRecordService.list());
    }

    @Operation(summary = "获取教师的监考记录", description = "获取指定教师在给定时间范围内的监考记录")
    @ApiResponse(responseCode = "200", description = "成功获取教师的监考记录", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InvigilationRecord.class))))
    @GetMapping("/records/teacher/{teacherId}")
    public Result<List<InvigilationRecord>> getTeacherRecords(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result
                    .success(invigilationRecordService.getTeacherRecordsByTimeRange(teacherId, startTime, endTime));
        }
        return Result.success(invigilationRecordService.getTeacherRecords(teacherId));
    }

    @Operation(summary = "获取监考统计信息", description = "获取指定时间范围内的监考统计信息")
    @ApiResponse(responseCode = "200", description = "成功获取监考统计信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, description = "包含监考统计信息的Map")))
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        return Result.success(invigilationRecordService.getInvigilationStatistics(startTime, endTime));
    }
}