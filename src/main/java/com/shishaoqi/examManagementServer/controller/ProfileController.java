package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignment;
import com.shishaoqi.examManagementServer.entity.invigilation.InvigilatorAssignmentStatus;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.service.InvigilatorAssignmentService;
import com.shishaoqi.examManagementServer.service.InvigilationRecordService;
import com.shishaoqi.examManagementServer.service.TrainingRecordService;
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
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.springframework.security.access.prepost.PreAuthorize;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherRole;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;

@Controller
@RequestMapping("/api/profile")
@PreAuthorize("hasAnyRole('ADMIN', 'EXAM_ADMIN', 'TEACHER')")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final TeacherService teacherService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final InvigilatorAssignmentService invigilatorAssignmentService;
    private final InvigilationRecordService invigilationRecordService;
    private final TrainingRecordService trainingRecordService;

    public ProfileController(
            TeacherService teacherService,
            BCryptPasswordEncoder passwordEncoder,
            InvigilatorAssignmentService invigilatorAssignmentService,
            InvigilationRecordService invigilationRecordService,
            TrainingRecordService trainingRecordService) {
        this.teacherService = teacherService;
        this.passwordEncoder = passwordEncoder;
        this.invigilatorAssignmentService = invigilatorAssignmentService;
        this.invigilationRecordService = invigilationRecordService;
        this.trainingRecordService = trainingRecordService;
    }

    @GetMapping
    public String getProfile(@AuthenticationPrincipal TeacherUserDetails userDetails, Model model) {
        try {
            log.info("开始获取教师个人信息");
            log.debug("当前用户角色: {}", userDetails != null ? userDetails.getAuthorities() : "未登录");
            log.debug("当前用户详情: {}", userDetails);

            if (userDetails == null) {
                log.error("用户未登录");
                model.addAttribute("error", "请先登录");
                return "profile";
            }

            // 获取教师信息
            Teacher teacher = null;
            if (userDetails.getTeacher() != null && userDetails.getTeacher().getTeacherId() != null) {
                log.debug("尝试获取教师ID为 {} 的信息", userDetails.getTeacher().getTeacherId());
                teacher = teacherService.getTeacherById(userDetails.getTeacher().getTeacherId());
                log.debug("获取到的教师信息: {}", teacher);
            }

            // 如果是管理员但没有找到教师信息，创建一个基本的教师对象
            if (teacher == null && userDetails.getAuthorities().stream()
                    .anyMatch(
                            a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EXAM_ADMIN"))) {
                teacher = new Teacher();
                if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    teacher.setName("系统管理员");
                    teacher.setRole(TeacherRole.ADMIN);
                } else {
                    teacher.setName("考务管理员");
                    teacher.setRole(TeacherRole.EXAM_ADMIN);
                }
                teacher.setStatus(TeacherStatus.ACTIVE);
                teacher.setEmail(userDetails.getUsername());
                log.debug("为管理员创建基本信息: {}", teacher);
            }

            if (teacher == null) {
                log.error("无法获取教师信息");
                model.addAttribute("error", "无法获取教师信息，请确保您已正确登录");
                return "profile";
            }

            // 添加基本信息
            model.addAttribute("teacher", teacher);
            log.debug("已添加教师基本信息到模型");

            // 如果是管理员，不需要获取监考和培训相关的统计信息
            if (teacher.getRole() != TeacherRole.ADMIN && teacher.getRole() != TeacherRole.EXAM_ADMIN) {
                try {
                    // 获取监考统计信息
                    int currentYear = LocalDateTime.now().getYear();
                    Map<String, Object> invigilationStats = new HashMap<>();
                    try {
                        log.debug("开始获取教师{}的监考统计信息", teacher.getTeacherId());
                        invigilationStats = invigilationRecordService
                                .getTeacherInvigilationStats(teacher.getTeacherId(), currentYear);
                        log.debug("获取到的监考统计信息: {}", invigilationStats);
                    } catch (Exception e) {
                        log.warn("获取监考统计信息失败", e);
                    }
                    if (invigilationStats == null) {
                        invigilationStats = new HashMap<>();
                    }
                    // 确保必要的统计数据存在
                    if (!invigilationStats.containsKey("totalRecords")) {
                        invigilationStats.put("totalRecords", 0);
                    }
                    if (!invigilationStats.containsKey("averageScore")) {
                        invigilationStats.put("averageScore", 0.0);
                    }
                    model.addAttribute("invigilationStats", invigilationStats);

                    // 获取监考历史记录
                    List<InvigilatorAssignment> assignments = new ArrayList<>();
                    try {
                        assignments = invigilatorAssignmentService
                                .getTeacherAssignments(teacher.getTeacherId());
                        if (assignments != null) {
                            assignments = assignments.stream()
                                    .filter(a -> a.getStatus() == InvigilatorAssignmentStatus.COMPLETED)
                                    .filter(a -> a.getExamStart() != null)
                                    .sorted((a1, a2) -> a2.getExamStart().compareTo(a1.getExamStart()))
                                    .collect(Collectors.toList());
                        }
                    } catch (Exception e) {
                        log.warn("获取监考历史记录失败", e);
                    }
                    model.addAttribute("invigilationHistory", assignments);

                    // 将工作量统计转换为按月份的格式
                    Map<String, Integer> monthlyStats = new HashMap<>();
                    for (int i = 1; i <= 12; i++) {
                        monthlyStats.put(String.valueOf(i), 0);
                    }

                    // 统计每月的监考次数
                    if (assignments != null) {
                        for (InvigilatorAssignment assignment : assignments) {
                            if (assignment.getExamStart() != null &&
                                    assignment.getExamStart().getYear() == currentYear) {
                                int month = assignment.getExamStart().getMonthValue();
                                String monthKey = String.valueOf(month);
                                monthlyStats.put(monthKey, monthlyStats.getOrDefault(monthKey, 0) + 1);
                            }
                        }
                    }
                    model.addAttribute("workloadStats", monthlyStats);

                    // 获取培训完成信息
                    Map<String, Object> trainingCompletion = new HashMap<>();
                    try {
                        trainingCompletion = trainingRecordService
                                .getRequiredTrainingStatus(teacher.getTeacherId());
                        if (trainingCompletion == null) {
                            trainingCompletion = new HashMap<>();
                        }
                    } catch (Exception e) {
                        log.warn("获取培训完成信息失败", e);
                    }
                    // 确保必要的培训数据存在
                    if (!trainingCompletion.containsKey("completionRate")) {
                        trainingCompletion.put("completionRate", 0.0);
                    }
                    model.addAttribute("trainingCompletion", trainingCompletion);

                    log.info("成功获取教师{}的个人信息", teacher.getName());
                    return "profile";
                } catch (Exception e) {
                    log.error("获取教师统计信息时发生错误", e);
                }
            } else {
                // 为管理员设置空的统计数据
                Map<String, Object> emptyStats = new HashMap<>();
                emptyStats.put("totalRecords", 0);
                emptyStats.put("averageScore", 0.0);
                model.addAttribute("invigilationStats", emptyStats);

                Map<String, Object> emptyTraining = new HashMap<>();
                emptyTraining.put("completionRate", 0.0);
                model.addAttribute("trainingCompletion", emptyTraining);

                model.addAttribute("invigilationHistory", new ArrayList<>());
                model.addAttribute("workloadStats", new HashMap<>());
            }

            return "profile";
        } catch (Exception e) {
            log.error("获取个人信息时发生系统异常", e);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "profile";
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