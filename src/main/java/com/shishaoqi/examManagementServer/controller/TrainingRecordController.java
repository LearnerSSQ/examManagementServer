package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "培训记录管理", description = "培训记录相关接口")
@RestController
@RequestMapping("/api/training-records")
public class TrainingRecordController {

    @Autowired
    private TrainingRecordService trainingRecordService;

    @Operation(summary = "获取教师的培训记录", description = "获取指定教师的所有培训记录列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取培训记录列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
    })
    @GetMapping("/teacher/{teacherId}")
    public List<TrainingRecord> getTeacherRecords(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
        return trainingRecordService.getTeacherRecords(teacherId);
    }

    @Operation(summary = "获取培训材料的学习记录", description = "获取指定培训材料的所有学习记录列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取学习记录列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
    })
    @GetMapping("/material/{materialId}")
    public List<TrainingRecord> getMaterialRecords(
            @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId) {
        return trainingRecordService.getMaterialRecords(materialId);
    }

    @Operation(summary = "检查教师是否完成培训", description = "检查指定教师是否完成指定培训材料的学习", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取完成状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/check-completion")
    public boolean hasCompletedTraining(
            @Parameter(description = "教师ID", required = true) @RequestParam Integer teacherId,
            @Parameter(description = "培训材料ID", required = true) @RequestParam Long materialId) {
        return trainingRecordService.hasCompletedTraining(teacherId, materialId);
    }
}