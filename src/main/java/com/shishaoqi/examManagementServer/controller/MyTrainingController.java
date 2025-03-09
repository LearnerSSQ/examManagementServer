package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecord;
import com.shishaoqi.examManagementServer.entity.training.TrainingRecordStatus;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import com.shishaoqi.examManagementServer.security.TeacherUserDetails;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "我的培训", description = "处理教师个人培训记录相关的接口")
@Controller
@RequestMapping("/api/training/my")
public class MyTrainingController {

    private static final Logger log = LoggerFactory.getLogger(MyTrainingController.class);

    @Autowired
    private TrainingRecordService trainingService;

    @Operation(summary = "获取我的培训列表", description = "获取当前登录教师的培训记录列表，支持状态筛选")
    @ApiResponse(responseCode = "200", description = "成功获取培训列表")
    @GetMapping
    public String getMyTrainings(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "培训状态") @RequestParam(required = false) TrainingRecordStatus status,
            Model model) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();
            List<TrainingRecord> trainings = trainingService.getTeacherTrainings(teacherId, status);

            model.addAttribute("trainings", trainings);
            return "my-training";
        } catch (BusinessException e) {
            log.error("获取培训列表时发生业务异常：{}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("获取培训列表时发生系统异常", e);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "error";
        }
    }

    @Operation(summary = "开始培训", description = "开始指定的培训课程")
    @ApiResponse(responseCode = "200", description = "成功开始培训")
    @PostMapping("/start/{recordId}")
    @ResponseBody
    public Result<Void> startTraining(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "培训记录ID", required = true) @PathVariable Long recordId) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();
            trainingService.startTraining(recordId, teacherId);
            return Result.success(null);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("开始培训时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }

    @Operation(summary = "访问培训学习页面", description = "进入指定培训的学习页面")
    @ApiResponse(responseCode = "200", description = "成功进入培训学习页面")
    @GetMapping("/learn/{recordId}")
    public String learnTraining(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "培训记录ID", required = true) @PathVariable Long recordId,
            Model model) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();
            TrainingRecord training = trainingService.getTrainingRecord(recordId, teacherId);

            model.addAttribute("training", training);
            return "training-learn";
        } catch (BusinessException e) {
            log.error("访问培训学习页面时发生业务异常：{}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("访问培训学习页面时发生系统异常", e);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "error";
        }
    }
    @PutMapping("/progress/{recordId}")
    @Operation(summary = "更新培训进度", description = "更新指定培训的学习进度")
    @ApiResponse(responseCode = "200", description = "成功更新培训进度")
    public Result<Void> updateProgress(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @PathVariable Long recordId,
            @RequestParam Integer progress) {
        try {
            if (progress < 0 || progress > 100) {
                return Result.error(ErrorCode.PARAM_ERROR, "进度值必须在0-100之间");
            }
            Integer teacherId = userDetails.getTeacher().getTeacherId();
            if (trainingService.updateProgress(recordId, teacherId, progress)) {
                return Result.success(null);
            }
            return Result.error(ErrorCode.PARAM_ERROR, "更新进度失败");
        } catch (BusinessException e) {
            log.error("更新培训进度时发生业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新培训进度时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }
}