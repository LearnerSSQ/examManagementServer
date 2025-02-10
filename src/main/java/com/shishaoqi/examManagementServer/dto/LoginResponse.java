package com.shishaoqi.examManagementServer.dto;

import com.shishaoqi.examManagementServer.entity.Teacher;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "用户信息")
    private Teacher teacher;

    public static LoginResponse of(String token, Teacher teacher) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTeacher(teacher);
        return response;
    }
}