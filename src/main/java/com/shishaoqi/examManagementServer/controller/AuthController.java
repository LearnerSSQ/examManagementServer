package com.shishaoqi.examManagementServer.controller;

import com.shishaoqi.examManagementServer.common.Result;
import com.shishaoqi.examManagementServer.dto.LoginRequest;
import com.shishaoqi.examManagementServer.dto.LoginResponse;
import com.shishaoqi.examManagementServer.entity.teacher.Teacher;
import com.shishaoqi.examManagementServer.service.TeacherService;
import com.shishaoqi.examManagementServer.util.JwtUtil;
import com.shishaoqi.examManagementServer.exception.ErrorCode;
import com.shishaoqi.examManagementServer.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "处理登录等认证相关的接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "教师登录", description = "使用邮箱和密码进行登录，返回JWT token和教师信息")
    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class, subTypes = {
            LoginResponse.class })))
    @PostMapping("/api-login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("收到登录请求");

            // 参数校验
            if (loginRequest == null) {
                log.warn("登录失败：请求体为空");
                return Result.error(ErrorCode.PARAM_ERROR, "请求参数不能为空");
            }

            log.info("登录请求参数，邮箱：{}，密码长度：{}",
                    loginRequest.getEmail(),
                    loginRequest.getPassword() != null ? loginRequest.getPassword().length() : 0);

            if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                log.warn("登录失败：邮箱或密码为空");
                return Result.error(ErrorCode.PARAM_ERROR, "邮箱和密码不能为空");
            }

            // 尝试登录
            log.debug("开始验证用户身份");
            Teacher teacher = null;
            try {
                teacher = teacherService.login(loginRequest.getEmail(), loginRequest.getPassword());
            } catch (BusinessException e) {
                log.warn("登录业务异常：{}，错误码：{}", e.getMessage(), e.getCode());
                return Result.error(e.getCode(), e.getMessage());
            }

            if (teacher == null) {
                log.error("登录失败：teacherService.login返回null");
                return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误：用户验证失败");
            }

            // 生成token
            log.debug("生成JWT token");
            String token = null;
            try {
                token = jwtUtil.generateToken(teacher.getEmail(), teacher.getTeacherId().longValue());
            } catch (Exception e) {
                log.error("生成token失败", e);
                return Result.error(ErrorCode.SYSTEM_ERROR, "生成token失败：" + e.getMessage());
            }

            if (token == null) {
                log.error("登录失败：生成的token为null");
                return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误：token生成失败");
            }

            log.info("登录成功，用户：{}，角色：{}", teacher.getName(), teacher.getRole());
            return Result.success(LoginResponse.of(token, teacher));

        } catch (Exception e) {
            log.error("登录过程发生未知错误", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "登录失败：" + e.getMessage());
        }
    }

    @Operation(summary = "验证token", description = "验证JWT token是否有效")
    @GetMapping("/verify")
    public Result<Boolean> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (!jwtUtil.isTokenExpired(token)) {
                    return Result.success(true);
                }
            }
            return Result.error(ErrorCode.UNAUTHORIZED, "无效的token");
        } catch (BusinessException e) {
            log.warn("Token验证失败：{}", e.getMessage());
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return Result.error(ErrorCode.SYSTEM_ERROR, "Token验证失败：" + e.getMessage());
        }
    }
}