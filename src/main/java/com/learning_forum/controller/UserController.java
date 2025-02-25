package com.learning_forum.controller;


import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.entity.User;
import com.learning_forum.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("users")

public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(200, "Success", userService.createUser(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<User>> getAllUsers() {
        ApiResponse<List<User>> apiResponse = new ApiResponse<>(200, "Success", userService.getAllUsers());
        return apiResponse;
    }

    @GetMapping("{userId}")
    UserResponse getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("{userId}")
    void deleteUser(@PathVariable String userId) {
        userService.deleteUserById(userId);
    }

    @PostMapping("{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(200, "Success", userService.updateUser(userId, request));
        return apiResponse;
    }
}
