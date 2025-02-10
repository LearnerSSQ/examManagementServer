package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.dto.LoginRequest;
import com.shishaoqi.examManagementServer.dto.LoginResponse;
import com.shishaoqi.examManagementServer.entity.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.util.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "教师登录", description = "使用邮箱和密码进行登录，返回JWT token和教师信息")
    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class, subTypes = {
            LoginResponse.class })))
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Teacher teacher = teacherService.login(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtil.generateToken(teacher.getEmail(), teacher.getTeacherId().longValue());
        return Result.success(LoginResponse.of(token, teacher));
    }
}