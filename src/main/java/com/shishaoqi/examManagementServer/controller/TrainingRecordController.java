package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.TrainingRecord;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
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
        public Result<List<TrainingRecord>> getTeacherRecords(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                return Result.success(trainingRecordService.getTeacherRecords(teacherId));
        }

        @Operation(summary = "获取培训材料的学习记录", description = "获取指定培训材料的所有学习记录列表", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取学习记录列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
        })
        @GetMapping("/material/{materialId}")
        public Result<List<TrainingRecord>> getMaterialRecords(
                        @Parameter(description = "培训材料ID", required = true) @PathVariable Long materialId) {
                return Result.success(trainingRecordService.getMaterialRecords(materialId));
        }

        @Operation(summary = "检查教师是否完成培训", description = "检查指定教师是否完成指定培训材料的学习", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取完成状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @GetMapping("/check-completion")
        public Result<Boolean> hasCompletedTraining(
                        @Parameter(description = "教师ID", required = true) @RequestParam Integer teacherId,
                        @Parameter(description = "培训材料ID", required = true) @RequestParam Long materialId) {
                return Result.success(trainingRecordService.hasCompletedTraining(teacherId, materialId));
        }

        @Operation(summary = "创建培训记录", description = "创建新的培训记录", responses = {
                        @ApiResponse(responseCode = "200", description = "成功创建培训记录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingRecord.class)))
        })
        @PostMapping
        public Result<TrainingRecord> createRecord(@RequestBody TrainingRecord record) {
                boolean success = trainingRecordService.saveOrUpdate(record);
                return success ? Result.success(record) : Result.error(ErrorCode.SYSTEM_ERROR);
        }

        @Operation(summary = "更新培训成绩", description = "更新指定培训记录的成绩和状态", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新培训成绩", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/{recordId}/score")
        public Result<Boolean> updateScore(
                        @Parameter(description = "记录ID", required = true) @PathVariable Long recordId,
                        @Parameter(description = "考试得分", required = true) @RequestParam Integer score,
                        @Parameter(description = "状态（0=未完成, 1=已完成, 2=已通过）", required = true) @RequestParam Integer status) {
                boolean success = trainingRecordService.updateScore(recordId, score, status);
                return success ? Result.success(true) : Result.error(ErrorCode.TRAINING_NOT_FOUND);
        }

        @Operation(summary = "更新学习进度", description = "更新培训记录的学习时长", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新学习进度", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/{recordId}/progress")
        public Result<Boolean> updateStudyProgress(
                        @Parameter(description = "记录ID", required = true) @PathVariable Long recordId,
                        @Parameter(description = "学习时长（分钟）", required = true) @RequestParam Integer studyTime) {
                boolean success = trainingRecordService.updateStudyProgress(recordId, studyTime);
                return success ? Result.success(true) : Result.error(ErrorCode.TRAINING_NOT_FOUND);
        }

        @Operation(summary = "验证学习时长", description = "验证是否达到规定学习时长", responses = {
                        @ApiResponse(responseCode = "200", description = "成功验证学习时长", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @GetMapping("/{recordId}/validate-time")
        public Result<Boolean> validateStudyTime(
                        @Parameter(description = "记录ID", required = true) @PathVariable Long recordId) {
                return Result.success(trainingRecordService.validateStudyTime(recordId));
        }
}