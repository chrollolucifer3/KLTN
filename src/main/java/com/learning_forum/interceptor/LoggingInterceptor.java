package com.learning_forum.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final String GREEN = "\u001B[32m"; // Màu xanh cho thành công
    private static final String RED = "\u001B[31m";   // Màu đỏ cho thất bại
    private static final String RESET = "\u001B[0m";  // Reset về mặc định

    private final ConcurrentHashMap<String, Long> requestTimeMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requestTimeMap.put(request.getRequestURI(), System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long endTime = System.currentTimeMillis();
        long startTime = requestTimeMap.getOrDefault(request.getRequestURI(), endTime);
        long duration = endTime - startTime; // Tính thời gian thực thi (ms)

        String color = (ex == null && response.getStatus() < 400) ? GREEN : RED;

        // Lấy tên method đang chạy
        String methodName = "UnknownMethod";
        if (handler instanceof HandlerMethod handlerMethod) {
            methodName = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        }

        // Log kết quả
        String logMessage = color + "API Completed: " + request.getMethod() + " " + request.getRequestURI() +
                " | Handler: " + methodName + " | Status: " + response.getStatus() +
                " | Time: " + (duration / 1000.0) + "s" + RESET;

        System.out.println(logMessage);
        requestTimeMap.remove(request.getRequestURI());
    }
}
