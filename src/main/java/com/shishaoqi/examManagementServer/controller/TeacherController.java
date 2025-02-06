package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
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
        public Result<List<Teacher>> getTeachersByDepartment(
                        @Parameter(description = "部门名称", required = true) @PathVariable String department) {
                return Result.success(teacherService.getTeachersByDepartment(department));
        }

        @Operation(summary = "根据邮箱获取教师信息", description = "通过教师邮箱查询教师详细信息", responses = {
                        @ApiResponse(responseCode = "200", description = "成功获取教师信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
        })
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

        @Operation(summary = "更新教师状态", description = "更新指定教师的状态", responses = {
                        @ApiResponse(responseCode = "200", description = "成功更新状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
        })
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
                boolean success = teacherService.updateLastLogin(teacherId);
                return success ? Result.success(true) : Result.error(ErrorCode.USER_NOT_FOUND);
        }
}