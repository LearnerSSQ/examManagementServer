package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "监考评价管理", description = "监考评价相关接口")
@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Operation(summary = "获取监考安排的评价", description = "获取指定监考安排的所有评价列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取评价列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Evaluation.class)))
    })
    @GetMapping("/assignment/{assignmentId}")
    public Result<List<Evaluation>> getEvaluationsByAssignment(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return Result.success(evaluationService.getEvaluationsByAssignment(assignmentId));
    }

    @Operation(summary = "获取教师的评价", description = "获取指定教师的所有评价列表", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取评价列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Evaluation.class)))
    })
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Evaluation>> getTeacherEvaluations(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
        return Result.success(evaluationService.getTeacherEvaluations(teacherId));
    }

    @Operation(summary = "获取教师的平均评分", description = "获取指定教师的所有评价的平均分", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取平均评分", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class)))
    })
    @GetMapping("/teacher/{teacherId}/average-score")
    public Result<BigDecimal> getTeacherAverageScore(
            @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
        return Result.success(evaluationService.getTeacherAverageScore(teacherId));
    }

    @Operation(summary = "获取评价数量", description = "获取指定监考安排的评价数量", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取评价数量", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)))
    })
    @GetMapping("/assignment/{assignmentId}/count")
    public Result<Integer> getEvaluationCount(
            @Parameter(description = "监考安排ID", required = true) @PathVariable Long assignmentId) {
        return Result.success(evaluationService.getEvaluationCount(assignmentId));
    }

    @Operation(summary = "检查是否已评价", description = "检查指定评价人是否已对指定监考安排进行评价", responses = {
            @ApiResponse(responseCode = "200", description = "成功获取评价状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/check")
    public Result<Boolean> hasEvaluated(
            @Parameter(description = "监考安排ID", required = true) @RequestParam Long assignmentId,
            @Parameter(description = "评价人ID", required = true) @RequestParam Integer evaluatorId) {
        return Result.success(evaluationService.hasEvaluated(assignmentId, evaluatorId));
    }

    @Operation(summary = "创建评价", description = "为指定监考安排创建新的评价", responses = {
            @ApiResponse(responseCode = "200", description = "成功创建评价", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Evaluation.class)))
    })
    @PostMapping
    public Result<Evaluation> createEvaluation(@RequestBody Evaluation evaluation) {
        boolean success = evaluationService.save(evaluation);
        return success ? Result.success(evaluation) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    @Operation(summary = "更新评价", description = "更新指定ID的评价", responses = {
            @ApiResponse(responseCode = "200", description = "成功更新评价", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Evaluation.class)))
    })
    @PutMapping("/{evaluationId}")
    public Result<Evaluation> updateEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long evaluationId,
            @RequestBody Evaluation evaluation) {
        evaluation.setEvaluationId(evaluationId);
        boolean success = evaluationService.updateById(evaluation);
        return success ? Result.success(evaluation) : Result.error(ErrorCode.NOT_FOUND);
    }

    @Operation(summary = "删除评价", description = "删除指定ID的评价", responses = {
            @ApiResponse(responseCode = "200", description = "成功删除评价", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    })
    @DeleteMapping("/{evaluationId}")
    public Result<Boolean> deleteEvaluation(
            @Parameter(description = "评价ID", required = true) @PathVariable Long evaluationId) {
        boolean success = evaluationService.removeById(evaluationId);
        return success ? Result.success(true) : Result.error(ErrorCode.NOT_FOUND);
    }
}