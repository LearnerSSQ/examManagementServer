package com.shishaoqi.examManagementServer.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.description = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.description = message;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.PARAM_ERROR.getCode();
        this.description = message;
    }
}