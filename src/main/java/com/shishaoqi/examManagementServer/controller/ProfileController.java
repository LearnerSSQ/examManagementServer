package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.message.Message;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
import com.shishaoqi.examManagementServer.service.MessageService;
import com.shishaoqi.examManagementServer.security.TeacherUserDetails;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.security.access.prepost.PreAuthorize;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

@Controller
@RequestMapping("/api/profile")
@PreAuthorize("hasAnyRole('ADMIN', 'EXAM_ADMIN', 'TEACHER')")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final TeacherService teacherService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final InvigilationRecordService invigilationRecordService;
    private final TrainingRecordService trainingRecordService;
    private final MessageService messageService;

    @Autowired
    public ProfileController(
            TeacherService teacherService,
            BCryptPasswordEncoder passwordEncoder,
            InvigilatorAssignmentService invigilatorAssignmentService,
            InvigilationRecordService invigilationRecordService,
            TrainingRecordService trainingRecordService,
            EvaluationService evaluationService,
            MessageService messageService) {
        this.teacherService = teacherService;
        this.passwordEncoder = passwordEncoder;
        this.invigilationRecordService = invigilationRecordService;
        this.trainingRecordService = trainingRecordService;
        this.messageService = messageService;
    }

    @GetMapping
    public String getProfile(@AuthenticationPrincipal TeacherUserDetails userDetails, Model model) {
        try {
            log.info("开始获取教师个人信息");

            if (userDetails == null || userDetails.getTeacher() == null) {
                log.error("用户未登录或教师信息不存在");
                return handleError(model, "请先登录");
            }

            Teacher teacher = userDetails.getTeacher();
            Integer teacherId = teacher.getTeacherId();

            if (teacherId == null) {
                log.error("教师ID为空");
                return handleError(model, "教师信息不完整");
            }

            model.addAttribute("teacher", teacher);

            // 对于管理员角色使用特殊处理
            if (teacher.getRole() == TeacherRole.ADMIN || teacher.getRole() == TeacherRole.EXAM_ADMIN) {
                return handleAdminProfile(model);
            }

            // 获取监考统计信息
            int currentYear = LocalDateTime.now().getYear();
            Map<String, Object> invigilationStats = invigilationRecordService
                    .getTeacherInvigilationStats(teacherId, currentYear);
            model.addAttribute("invigilationStats", invigilationStats);

            // 获取监考历史记录
            List<Map<String, Object>> invigilationHistory = invigilationRecordService
                    .getTeacherInvigilationHistory(teacherId);
            model.addAttribute("invigilationHistory", invigilationHistory);

            // 获取培训完成情况
            Map<String, Object> trainingCompletion = trainingRecordService
                    .getRequiredTrainingStatus(teacherId);
            model.addAttribute("trainingCompletion", trainingCompletion);

            // 获取工作量统计
            Map<String, Integer> workloadStats = calculateWorkloadStats(invigilationHistory);
            model.addAttribute("workloadStats", workloadStats);

            // 获取用户消息
            List<Message> recentMessages = messageService.getTeacherMessages(teacherId);
            model.addAttribute("messages", recentMessages);

            // 获取未读消息数量
            int unreadCount = messageService.getUnreadCount(teacherId);
            model.addAttribute("unreadCount", unreadCount);

            log.info("成功获取教师{}的个人信息", teacher.getName());
            return "profile";

        } catch (Exception e) {
            log.error("获取个人信息时发生系统异常", e);
            return handleError(model, "系统错误，请稍后重试");
        }
    }

    private String handleAdminProfile(Model model) {
        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("totalRecords", 0);
        emptyStats.put("averageScore", 0.0);

        Map<String, Object> emptyTraining = new HashMap<>();
        emptyTraining.put("completionRate", 0.0);

        model.addAttribute("invigilationStats", emptyStats);
        model.addAttribute("trainingCompletion", emptyTraining);
        model.addAttribute("invigilationHistory", new ArrayList<>());
        model.addAttribute("workloadStats", new HashMap<>());
        model.addAttribute("messages", new ArrayList<>());
        model.addAttribute("unreadCount", 0);

        return "profile";
    }

    private String handleError(Model model, String message) {
        model.addAttribute("error", message);
        return "profile";
    }

    private Map<String, Integer> calculateWorkloadStats(List<Map<String, Object>> history) {
        return calculateWorkloadStats(history, LocalDateTime.now().getYear());
    }

    private Map<String, Integer> calculateWorkloadStats(List<Map<String, Object>> history, int year) {
        Map<String, Integer> monthlyStats = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyStats.put(String.valueOf(i), 0);
        }

        if (history != null) {
            history.stream()
                    .map(record -> (LocalDateTime) record.get("examTime")) // 修改这里，与返回的数据结构对应
                    .filter(Objects::nonNull)
                    .filter(date -> date.getYear() == year)
                    .forEach(date -> {
                        String month = String.valueOf(date.getMonthValue());
                        monthlyStats.merge(month, 1, Integer::sum);
                    });
        }

        return monthlyStats;
    }

    @GetMapping("/workload-stats")
    @ResponseBody
    public Result<Map<String, Integer>> getWorkloadStats(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @RequestParam(required = false) Integer year) {
        try {
            if (userDetails == null || userDetails.getTeacher() == null) {
                return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "请先登录");
            }

            Teacher teacher = userDetails.getTeacher();
            Integer teacherId = teacher.getTeacherId();

            if (teacherId == null) {
                return Result.error(ErrorCode.PARAM_ERROR.getCode(), "教师信息不完整");
            }

            // 如果未指定年份，使用当前年份
            if (year == null) {
                year = LocalDateTime.now().getYear();
            }

            // 获取指定年份的监考历史记录
            List<Map<String, Object>> invigilationHistory = invigilationRecordService
                    .getTeacherInvigilationHistoryByYear(teacherId, year);

            // 计算工作量统计
            Map<String, Integer> workloadStats = calculateWorkloadStats(invigilationHistory, year);

            return Result.success(workloadStats);
        } catch (Exception e) {
            log.error("获取工作量统计数据失败", e);
            return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误，请稍后重试");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<Teacher> updateProfile(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @RequestBody Teacher updateInfo) {
        try {
            log.info("开始更新教师个人信息");

            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Teacher teacher = userDetails.getTeacher();
            if (teacher == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "教师信息不存在");
            }

            // 只允许更新部分字段
            teacher.setPhone(updateInfo.getPhone());
            teacher.setEmail(updateInfo.getEmail());

            boolean success = teacherService.updateTeacher(teacher);
            if (success) {
                log.info("教师{}信息更新成功", teacher.getName());
                return Result.success(teacher);
            } else {
                log.error("教师{}信息更新失败", teacher.getName());
                return Result.error(ErrorCode.SYSTEM_ERROR, "更新失败");
            }

        } catch (BusinessException e) {
            log.error("更新个人信息时发生业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新个人信息时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }

    @PostMapping("/change-password")
    @ResponseBody
    public Result<Void> changePassword(
            @AuthenticationPrincipal TeacherUserDetails userDetails,
            @RequestBody Map<String, String> passwordData) {
        try {
            log.info("开始处理修改密码请求");

            if (userDetails == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }

            Teacher teacher = userDetails.getTeacher();
            if (teacher == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "教师信息不存在");
            }

            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码参数不能为空");
            }

            // 验证旧密码
            if (!teacherService.verifyPassword(oldPassword, teacher.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_ERROR, "当前密码错误");
            }

            // 验证新密码格式
            if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码必须包含至少8个字符，至少1个字母和1个数字");
            }

            // 对新密码进行加密
            String encodedPassword = passwordEncoder.encode(newPassword);
            teacher.setPassword(encodedPassword);

            boolean success = teacherService.updateTeacher(teacher);

            if (success) {
                log.info("教师{}密码修改成功", teacher.getName());
                return Result.success(null);
            } else {
                log.error("教师{}密码修改失败", teacher.getName());
                return Result.error(ErrorCode.SYSTEM_ERROR, "密码修改失败");
            }

        } catch (BusinessException e) {
            log.error("修改密码时发生业务异常：{}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改密码时发生系统异常", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误，请稍后重试");
        }
    }
}