package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.entity.Evaluation;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "教师管理", description = "教师信息管理相关接口")
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

        @Autowired
        private TeacherService teacherService;

        @Autowired
        private EvaluationService evaluationService;

        @Operation(summary = "根据部门获取教师列表", description = "获取指定部门的所有教师信息")
        @ApiResponse(responseCode = "200", description = "成功获取教师列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Teacher.class))))
        @GetMapping("/department/{department}")
        public Result<List<Teacher>> getTeachersByDepartment(
                        @Parameter(description = "部门名称", required = true) @PathVariable String department) {
                return Result.success(teacherService.getTeachersByDepartment(department));
        }

        @Operation(summary = "根据邮箱获取教师信息", description = "通过教师邮箱查询教师详细信息")
        @ApiResponse(responseCode = "200", description = "成功获取教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        @GetMapping("/email/{email}")
        public Result<Teacher> getTeacherByEmail(
                        @Parameter(description = "教师邮箱", required = true) @PathVariable String email) {
                Teacher teacher = teacherService.getTeacherByEmail(email);
                return teacher != null ? Result.success(teacher) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "根据手机号获取教师信息", description = "通过教师手机号查询教师详细信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
        @GetMapping("/phone/{phone}")
        public Result<Teacher> getTeacherByPhone(
                        @Parameter(description = "教师手机号", required = true) @PathVariable String phone) {
                Teacher teacher = teacherService.getTeacherByPhone(phone);
                return teacher != null ? Result.success(teacher) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "更新教师信息", description = "更新指定教师的详细信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
        @PutMapping("/{teacherId}")
        public Result<Teacher> updateTeacher(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
                        @RequestBody Teacher teacher) {
                teacher.setTeacherId(teacherId);
                boolean success = teacherService.updateById(teacher);
                return success ? Result.success(teacher) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "更新教师状态", description = "更新指定教师的状态")
        @ApiResponse(responseCode = "200", description = "成功更新状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        @PutMapping("/{teacherId}/status/{status}")
        public Result<Boolean> updateStatus(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
                        @Parameter(description = "状态（0=未激活, 1=正常, 2=禁用）", required = true) @PathVariable Integer status) {
                boolean success = teacherService.updateStatus(teacherId, status);
                return success ? Result.success(true) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "更新最后登录时间", description = "更新指定教师的最后登录时间", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新登录时间", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/{teacherId}/last-login")
        public Result<Boolean> updateLastLogin(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId) {
                boolean success = (boolean) teacherService.updateLastLogin(teacherId);
                return success ? Result.success(true) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "更新教师职称", description = "更新指定教师的职称", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新职称", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
        @PutMapping("/{teacherId}/title")
        public Result<Boolean> updateTitle(
                        @Parameter(description = "教师ID", required = true) @PathVariable Integer teacherId,
                        @Parameter(description = "新职称", required = true) @RequestParam String title) {
                boolean success = teacherService.updateTitle(teacherId, title);
                return success ? Result.success(true) : Result.error(ErrorCode.USER_NOT_FOUND);
        }

        @Operation(summary = "提交教师评估")
        @PostMapping("/{teacherId}/evaluations")
        public Result<Boolean> submitEvaluation(
                        @PathVariable Integer teacherId,
                        @RequestParam Long assignmentId,
                        @RequestParam Integer evaluatorId,
                        @RequestParam Double score,
                        @RequestParam String comment) {
                boolean success = evaluationService.createEvaluation(assignmentId, evaluatorId, score, comment);
                return Result.success(success);
        }

        @Operation(summary = "获取教师评估列表", description = "获取指定教师的所有评估记录")
        @ApiResponse(responseCode = "200", description = "成功获取评估列表", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Evaluation.class))))
        @GetMapping("/{teacherId}/evaluations")
        public Result<List<Evaluation>> getTeacherEvaluations(@PathVariable Integer teacherId) {
                return Result.success(evaluationService.getTeacherEvaluations(teacherId));
        }

        @Operation(summary = "获取教师评估统计", description = "获取指定教师在给定时间范围内的评估统计信息")
        @ApiResponse(responseCode = "200", description = "成功获取评估统计", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, description = "包含评估统计信息的Map")))
        @GetMapping("/{teacherId}/evaluation-stats")
        public Result<Map<String, Object>> getEvaluationStats(
                        @PathVariable Integer teacherId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
                if (startTime == null) {
                        startTime = LocalDateTime.now().minusYears(1);
                }
                if (endTime == null) {
                        endTime = LocalDateTime.now();
                }
                return Result.success(evaluationService.getTeacherEvaluationStats(teacherId, startTime, endTime));
        }
}