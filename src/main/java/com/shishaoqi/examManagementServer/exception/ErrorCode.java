package com.shishaoqi.examManagementServer.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 通用错误码
    SUCCESS(200, "成功"),
    SYSTEM_ERROR(5000, "系统错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    OPERATION_NOT_ALLOWED(403, "操作不允许"),

    // 用户相关错误码 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),
    ACCOUNT_EXPIRED(1005, "账号已过期"),
    TOKEN_EXPIRED(1006, "token已过期"),
    TOKEN_INVALID(1007, "token无效"),

    // 培训相关错误码 (2000-2999)
    TRAINING_NOT_FOUND(2000, "培训记录不存在"),
    TRAINING_ALREADY_COMPLETED(2001, "培训已完成"),
    TRAINING_EXPIRED(2002, "培训已过期"),
    INSUFFICIENT_STUDY_TIME(2003, "学习时长不足"),

    // 监考相关错误码 (3000-3999)
    ASSIGNMENT_NOT_FOUND(3000, "监考安排不存在"),
    ASSIGNMENT_ALREADY_CONFIRMED(3001, "监考已确认"),
    ASSIGNMENT_ALREADY_CANCELED(3002, "监考已取消"),
    ASSIGNMENT_TIME_CONFLICT(3003, "监考时间冲突"),

    // 消息相关错误码 (4000-4999)
    MESSAGE_NOT_FOUND(4000, "消息不存在"),
    MESSAGE_ALREADY_READ(4001, "消息已读"),
    INVALID_TEACHER_ID(4002, "无效的教师ID"),

    // 评价相关错误码 (5000-5999)
    EVALUATION_NOT_FOUND(5000, "评价不存在"),
    EVALUATION_ALREADY_EXISTS(5001, "评价已存在"),
    INVALID_SCORE(5002, "无效的评分"),

    // 监考相关错误码
    ASSIGNMENT_NOT_FOUND_CODE(4001, "监考安排不存在"),
    ASSIGNMENT_ALREADY_CONFIRMED_CODE(4002, "监考已确认"),
    ASSIGNMENT_ALREADY_CANCELED_CODE(4003, "监考已取消"),

    // 培训相关错误码
    TRAINING_NOT_FOUND_CODE(4101, "培训记录不存在"),
    TRAINING_ALREADY_STARTED(4102, "培训已开始");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}