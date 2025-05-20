package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.AuthenticationRequest;
import com.learning_forum.dto.request.LogoutRequest;
import com.learning_forum.dto.request.RefreshRequest;
import com.learning_forum.dto.respone.AuthAdminResponse;
import com.learning_forum.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("admin")
public class AuthAdminController {

    AuthService authService;

    // Login
    @PostMapping("/login")
    public ApiResponse<AuthAdminResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        log.info("#AuthAdminController.Login with {}", request);
        return ApiResponse.<AuthAdminResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.loginAdmin(request))
                .build();
    }

    // Refresh token
    @PostMapping("/refresh")
    public ApiResponse<AuthAdminResponse> refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        log.info("#AuthAdminController.RefreshToken with {}", request);
        return ApiResponse.<AuthAdminResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.refreshTokenAdmin(request))
                .build();
    }

    // Logout
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        log.info("#AuthAdminController.Logout with {}", request);
        authService.logout(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }
}
