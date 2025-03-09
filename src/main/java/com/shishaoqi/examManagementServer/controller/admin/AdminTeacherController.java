package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.common.PageResult;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.entity.teacher.TeacherStatus;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@Controller
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private static final Logger log = LoggerFactory.getLogger(AdminTeacherController.class);
    private final TeacherService teacherService;

    public AdminTeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String teachersPage(Model model) {
        List<Teacher> teachers = teacherService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        // 添加TeacherStatus枚举值到模型
        model.addAttribute("TeacherStatus", TeacherStatus.values());
        return "admin/teachers";
    }

    @GetMapping("/{teacherId}")
    @ResponseBody
    public Result<Teacher> getTeacher(@PathVariable Integer teacherId) {
        try {
            Teacher teacher = teacherService.getTeacherById(teacherId);
            if (teacher != null) {
                return Result.success(teacher);
            } else {
                return Result.error(404, "教师不存在");
            }
        } catch (Exception e) {
            log.error("获取教师信息失败", e);
            return Result.error(500, "获取教师信息失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public Result<PageResult<Teacher>> listTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码从1开始
            page = Math.max(0, page);
            size = Math.max(1, Math.min(size, 100)); // 限制每页最大100条

            PageResult<Teacher> pageResult = teacherService.getTeachersByPage(page + 1, size);

            // 返回分页结果
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取教师列表失败", e);
            return Result.error(500, "获取教师列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/search")
    @ResponseBody
    public Result<List<Teacher>> searchTeachers(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error(400, "搜索关键字不能为空");
            }
            List<Teacher> teachers = teacherService.list(keyword);
            return Result.success(teachers);
        } catch (Exception e) {
            log.error("搜索教师失败", e);
            return Result.error(500, "搜索教师失败：" + e.getMessage());
        }
    }

    @PostMapping
    @ResponseBody
    public Result<Teacher> addTeacher(@RequestBody Teacher teacher) {
        try {
            // 设置默认密码
            teacher.setPassword("123456");
            boolean success = teacherService.addTeacher(teacher);
            if (success) {
                return Result.success(teacher);
            } else {
                return Result.error(500, "添加教师失败");
            }
        } catch (Exception e) {
            log.error("添加教师失败", e);
            return Result.error(500, "添加教师失败：" + e.getMessage());
        }
    }

    @PutMapping("/{teacherId}")
    @ResponseBody
    public Result<Teacher> updateTeacher(@PathVariable Integer teacherId, @RequestBody Teacher teacher) {
        try {
            teacher.setTeacherId(teacherId);
            boolean success = teacherService.updateTeacher(teacher);
            if (success) {
                return Result.success(teacher);
            } else {
                return Result.error(500, "更新教师失败");
            }
        } catch (Exception e) {
            log.error("更新教师失败", e);
            return Result.error(500, "更新教师失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{teacherId}")
    @ResponseBody
    public Result<Void> deleteTeacher(@PathVariable Integer teacherId) {
        try {
            boolean success = teacherService.deleteTeacher(teacherId);
            if (success) {
                return Result.success("删除教师成功");
            } else {
                return Result.error(500, "删除教师失败");
            }
        } catch (Exception e) {
            log.error("删除教师失败", e);
            return Result.error(500, "删除教师失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @ResponseBody
    public Result<Void> batchDeleteTeachers(@RequestBody List<Integer> teacherIds) {
        try {
            if (teacherIds == null || teacherIds.isEmpty()) {
                return Result.error(400, "请选择要删除的教师");
            }
            boolean success = teacherService.batchDeleteTeachers(teacherIds);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error(500, "批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除失败", e);
            return Result.error(500, "批量删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/batch-update-status")
    @ResponseBody
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> teacherIds = (List<Integer>) request.get("ids");
            String status = request.get("status").toString();

            if (teacherIds == null || teacherIds.isEmpty()) {
                return Result.error(400, "请选择要操作的教师");
            }
            if (status == null) {
                return Result.error(400, "状态不能为空");
            }

            boolean success = teacherService.batchUpdateStatus(teacherIds, status);
            if (success) {
                return Result.success("批量更新状态成功");
            } else {
                return Result.error(500, "批量更新状态失败");
            }
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            return Result.error(500, "批量更新状态失败：" + e.getMessage());
        }
    }
}