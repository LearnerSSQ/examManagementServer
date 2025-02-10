package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.TrainingMaterial;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.service.TrainingMaterialService;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
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

@Tag(name = "培训管理", description = "处理培训材料和记录相关的接口")
@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingMaterialService trainingMaterialService;

    @Autowired
    private TrainingRecordService trainingRecordService;

    @Operation(summary = "上传培训材料", description = "创建新的培训材料")
    @ApiResponse(responseCode = "200", description = "成功上传培训材料", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    @PostMapping("/materials")
    public Result<TrainingMaterial> uploadMaterial(@RequestBody TrainingMaterial material) {
        trainingMaterialService.save(material);
        return Result.success(material);
    }

    @Operation(summary = "获取培训材料列表", description = "获取所有培训材料")
    @ApiResponse(responseCode = "200", description = "成功获取培训材料列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingMaterial.class))))
    @GetMapping("/materials")
    public Result<List<TrainingMaterial>> listMaterials() {
        return Result.success(trainingMaterialService.list());
    }

    @Operation(summary = "获取培训材料详情", description = "获取指定ID的培训材料详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取培训材料详情", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingMaterial.class)))
    @GetMapping("/materials/{id}")
    public Result<TrainingMaterial> getMaterial(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Integer id) {
        return Result.success(trainingMaterialService.getById(id));
    }

    @Operation(summary = "创建培训记录", description = "创建新的培训学习记录")
    @ApiResponse(responseCode = "200", description = "成功创建培训记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
    @PostMapping("/records")
    public Result<TrainingRecord> createRecord(@RequestBody TrainingRecord record) {
        trainingRecordService.save(record);
        return Result.success(record);
    }

    @Operation(summary = "获取培训记录列表", description = "获取指定时间范围内的培训记录列表")
    @ApiResponse(responseCode = "200", description = "成功获取培训记录列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingRecord.class))))
    @GetMapping("/records")
    public Result<List<TrainingRecord>> listRecords(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(trainingRecordService.getByTimeRange(startTime, endTime));
        }
        return Result.success(trainingRecordService.list());
    }

    @Operation(summary = "获取教师的培训记录", description = "获取指定教师在给定时间范围内的培训记录")
    @ApiResponse(responseCode = "200", description = "成功获取教师的培训记录", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TrainingRecord.class))))
    @GetMapping("/records/teacher/{teacherId}")
    public Result<List<TrainingRecord>> getTeacherRecords(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return Result.success(trainingRecordService.getTeacherRecordsByTimeRange(teacherId, startTime, endTime));
        }
        return Result.success(trainingRecordService.getTeacherRecords(teacherId));
    }

    @Operation(summary = "获取培训统计信息", description = "获取指定时间范围内的培训统计信息")
    @ApiResponse(responseCode = "200", description = "成功获取培训统计信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, description = "包含培训统计信息的Map")))
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        return Result.success(trainingRecordService.getTrainingStatistics(startTime, endTime));
    }
}