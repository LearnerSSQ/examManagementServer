package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教师管理", description = "教师信息管理相关接口")
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

        @Autowired
        private TeacherService teacherService;

        @Operation(summary = "根据部门获取教师列表", description = "获取指定部门的所有教师信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取教师列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
        @GetMapping("/department/{department}")
        public List<Teacher> getTeachersByDepartment(
                        @Parameter(description = "部门名称", required = true) @PathVariable String department) {
                return teacherService.getTeachersByDepartment(department);
        }

        @Operation(summary = "根据邮箱获取教师信息", description = "通过教师邮箱查询教师详细信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
        @GetMapping("/email/{email}")
        public Teacher getTeacherByEmail(
                        @Parameter(description = "教师邮箱", required = true) @PathVariable String email) {
                return teacherService.getTeacherByEmail(email);
        }

        @Operation(summary = "根据手机号获取教师信息", description = "通过教师手机号查询教师详细信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
        @GetMapping("/phone/{phone}")
        public Teacher getTeacherByPhone(
                        @Parameter(description = "教师手机号", required = true) @PathVariable String phone) {
                return teacherService.getTeacherByPhone(phone);
        }
}