package com.learning_forum.controller;

import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.AuthenticationResponse;
import com.learning_forum.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {

    AuthService authService;

    // Login
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        log.info("AuthController.Login with request: {}", request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.login(request))
                .build();
    }

    // Logout
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        log.info("AuthController.Logout with request: {}", request);
        authService.logout(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Refresh token
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        log.info("AuthController.Refresh with request: {}", request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.refreshToken(request))
                .build();
    }

    // Update password
    @PostMapping("/updatePassword/{id}")
    public ApiResponse<?> updatePassword(@PathVariable String id, @RequestBody @Valid UserUpdatePasswordRequest request) {
        log.info("AuthController.UpdatePassword with request: {}", request);
        authService.updatePassword(id, request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }
}
