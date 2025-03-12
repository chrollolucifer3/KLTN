package com.learning_forum.exception;

import com.learning_forum.dto.request.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobalExceptionHandler {

    // Xử lý các lỗi business (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ex.getErrorCode().getCode());
        apiResponse.setMessage(ex.getErrorCode().getMessage());
        if (ex.getErrors() != null) {
            apiResponse.setErrors(ex.getErrors());
        }
        return ResponseEntity.status(ex.getErrorCode().getCode()).body(apiResponse);
    }

    // Handler cho các lỗi validate từ @Valid (nếu cần)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException exception) {
        var errorMap = exception.getBindingResult().getFieldErrors()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setMessage("Validation failed");
        apiResponse.setErrors(errorMap);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

//    @ExceptionHandler(value = AppException.class)
//    ResponseEntity<ApiResponse> handleAppException(AppException exception) {
//        ErrorCode errorCode = exception.getErrorCode();
//
//        ApiResponse apiResponse = new ApiResponse();
//
//        apiResponse.setMessage(exception.getMessage());
//        apiResponse.setCode(errorCode.getCode());
//
//        return ResponseEntity.status(errorCode.getCode()).body(apiResponse);
//    }
//
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    ResponseEntity<ApiResponse> handleValidation (MethodArgumentNotValidException exception) {
//
//        String enumKey = exception.getFieldError().getDefaultMessage();
//        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.setMessage(errorCode.getMessage());
//        apiResponse.setCode(errorCode.getCode());
//
//        return ResponseEntity.status(errorCode.getCode()).body(apiResponse);
//    }
}
