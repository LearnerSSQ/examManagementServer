package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
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

@Tag(name = "我的监考", description = "处理教师个人监考任务相关的接口")
@Controller
@RequestMapping("/api/assignments/my")
public class MyAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(MyAssignmentController.class);

    @Autowired
    private InvigilatorAssignmentService assignmentService;

    @Operation(summary = "获取我的监考任务列表", description = "获取当前登录教师的监考任务列表，支持状态和日期筛选")
    @ApiResponse(responseCode = "200", description = "成功获取监考任务列表")
    @GetMapping
    public String getMyAssignments(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "监考状态") @RequestParam(required = false) InvigilatorAssignmentStatus status,
            @Parameter(description = "开始日期 (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            Model model) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();

            // 获取监考任务列表
            List<InvigilatorAssignment> assignments = assignmentService.getTeacherAssignments(
                    teacherId, status, startDate, endDate);

            log.info("获取到教师{}的监考任务{}条", teacherId, assignments.size());

            model.addAttribute("assignments", assignments);
            return "my-assignments";
        } catch (BusinessException e) {
            log.error("获取监考列表时发生业务异常：{}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("获取监考列表时发生系统异常", e);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "error";
        }
    }

    @Operation(summary = "确认监考任务", description = "确认接受指定的监考任务")
    @ApiResponse(responseCode = "200", description = "成功确认监考任务")
    @PostMapping("/confirm/{assignmentId}")
    @ResponseBody
    public Result<Void> confirmAssignment(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "监考任务ID", required = true) @PathVariable Long assignmentId) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();
            assignmentService.confirmAssignment(assignmentId, teacherId);
            return Result.success(null);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("确认监考任务时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }

    @Operation(summary = "取消监考任务", description = "取消指定的监考任务")
    @ApiResponse(responseCode = "200", description = "成功取消监考任务")
    @PostMapping("/cancel/{assignmentId}")
    @ResponseBody
    public Result<Void> cancelAssignment(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @Parameter(description = "监考任务ID", required = true) @PathVariable Long assignmentId) {
        try {
            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Integer teacherId = userDetails.getTeacher().getTeacherId();
            assignmentService.cancelAssignment(assignmentId, teacherId);
            return Result.success(null);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("取消监考任务时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }
}