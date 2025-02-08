package com.shishaoqi.examManagementServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码")
    private String password;
}