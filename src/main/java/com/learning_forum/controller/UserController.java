package com.learning_forum.controller;


import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.AuthenticationRequest;
import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.AuthenticationResponse;
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

    // Create user
    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return new ApiResponse<>(200, "Success", userService.createUser(request));
    }

    // Get all users
    @GetMapping
    ApiResponse<List<User>> getAllUsers() {
        return new ApiResponse<>(200, "Success", userService.getAllUsers());
    }

    // Get user by id
    @GetMapping("{userId}")
    UserResponse getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    // Delete user
    @DeleteMapping("{userId}")
    void deleteUser(@PathVariable String userId) {
        userService.deleteUserById(userId);
    }

    // Update user
    @PostMapping("{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return new ApiResponse<>(200, "Success", userService.updateUser(userId, request));
    }
}
