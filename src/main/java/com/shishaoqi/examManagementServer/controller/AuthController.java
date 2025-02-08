package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.dto.LoginRequest;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "处理登录等认证相关的接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TeacherService teacherService;

    @Operation(summary = "教师登录", description = "使用邮箱和密码进行登录", responses = {
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Teacher.class)))
    })
    @PostMapping("/login")
    public Result<Teacher> login(@RequestBody LoginRequest loginRequest) {
        Teacher teacher = teacherService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return Result.success(teacher);
    }
}