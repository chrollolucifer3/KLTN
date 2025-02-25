package com.learning_forum.exception;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;


import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AppException extends RuntimeException {
     ErrorCode errorCode;
     Map<String, String> errors; // Có thể null nếu không có lỗi field cụ thể

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = null;
    }

    public AppException(ErrorCode errorCode, Map<String, String> errors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
