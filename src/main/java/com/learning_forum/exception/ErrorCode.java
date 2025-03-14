package com.learning_forum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    //USER
    USER_NOT_FOUND(404, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND ),
    USER_ALREADY_EXISTED(409, "Tài khoản đã tồn tại", HttpStatus.CONFLICT),
    USERNAME_INVALID(400, "Username is invalid", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "Password is invalid", HttpStatus.BAD_REQUEST),
    USER_BLOCKED(403, "Tài khoản đã bị khóa" , HttpStatus.FORBIDDEN),
    USER_UNBLOCKED(403, "Tài khoản chưa bị khóa", HttpStatus.FORBIDDEN),
    SUPER_ADMIN_BLOCKED(403, "Bạn không thể khóa tài khoản này", HttpStatus.FORBIDDEN),
    //Valid
    VALIDATION_FAILED(400, "Validation failed", HttpStatus.BAD_REQUEST),
    //Server
    INTERNAL_SERVER_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    //AUTHORIZED
    UNAUTHORIZED(401, "Bạn không có quyền truy cập" , HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(401, "Bạn chưa đăng nhập", HttpStatus.UNAUTHORIZED),
    UNCAUGHT_EXCEPTION(500, "Uncaught exception", HttpStatus.INTERNAL_SERVER_ERROR),

    //FILE
    FILE_UPLOAD_FAILED(400, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY(400, "Ảnh không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(400, "Định dạng file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(400, "Dung lượng file Không được vượt quá 5MB", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(400, "Token không hợp lệ" , HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}