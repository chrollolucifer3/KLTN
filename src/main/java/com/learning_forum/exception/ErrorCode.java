package com.learning_forum.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //USER
    USER_NOT_FOUND(404, "Tài khoản không tồn tại"),
    USER_ALREADY_EXISTED(409, "Tài khoản đã tồn tại"),
    USERNAME_INVALID(400, "Username is invalid"),
    INVALID_PASSWORD(400, "Password is invalid"),
    USER_BLOCKED(403, "Tài khoản đã bị khóa"),
    USER_UNBLOCKED(403, "Tài khoản chưa bị khóa"),
    SUPER_ADMIN_BLOCKED(403, "Bạn không thể khóa tài khoản này"),
    //Valid
    VALIDATION_FAILED(400, "Validation failed"),
    //Server
    INTERNAL_SERVER_ERROR(500, "Internal server error"),

    //AUTHORIZED
    UNAUTHORIZED(401, "Unauthorized"),
    UNCAUGHT_EXCEPTION(500, "Uncaught exception"),
    ;
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}