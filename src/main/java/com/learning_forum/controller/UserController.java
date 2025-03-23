package com.learning_forum.controller;


import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.UserListResponse;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.dto.respone.UserResponseForAdmin;
import com.learning_forum.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping("getAll")
    ApiResponse<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String search
    ) {
        UserListResponse users = userService.getAllUsers(page, size, sortBy, search);
        return new ApiResponse<>(200, "Success", users);
    }

    // Get my profile
    @GetMapping("myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return new ApiResponse<>(200, "Success", userService.getMyInfo());
    }

    // Update user
    @PostMapping("{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return new ApiResponse<>(200, "Success", userService.updateUser(userId, request));
    }

    // Block user
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("block/{userId}")
    ApiResponse<?> blockUser(@PathVariable String userId) {
        userService.blockUser(userId);
        return new ApiResponse<>(200, "Success");
    }

    // Unblock user
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("unblock/{userId}")
    ApiResponse<?> unblockUser(@PathVariable String userId) {
        userService.unblockUser(userId);
        return new ApiResponse<>(200, "Success");
    }

    // upload avatar
    @PostMapping("avatar/{userId}")
    ApiResponse<?> uploadAvatar(@PathVariable String userId, @RequestParam("avatarUrl") MultipartFile file) {
        userService.uploadAvatar(userId, file);
        return new ApiResponse<>(200, "Success");
    }

}
