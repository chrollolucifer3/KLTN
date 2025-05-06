package com.learning_forum.controller;

import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.AuthenticationResponse;
import com.learning_forum.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {

    AuthService authService;

    // Login
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.login(request))
                .build();
    }

    // Logout
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authService.logout(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Refresh token
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Success")
                .result(authService.refreshToken(request))
                .build();
    }

    // Update password
    @PostMapping("/updatePassword/{id}")
    public ApiResponse<?> updatePassword(@PathVariable String id, @RequestBody @Valid UserUpdatePasswordRequest request) {
        authService.updatePassword(id, request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }
}
