package com.learning_forum.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
    private Map<String, String> errors;

    // Constructor có tham số
    public ApiResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }
}
