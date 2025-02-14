package com.shishaoqi.examManagementServer.controller.admin;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}