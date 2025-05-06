package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.ApprovePostRequest;
import com.learning_forum.dto.request.PostRequest;
import com.learning_forum.dto.request.RejectPostRequest;
import com.learning_forum.dto.respone.ListPostResponse;
import com.learning_forum.dto.respone.UserListResponse;
import com.learning_forum.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("post")
public class PostController {

    PostService postService;

    @PostMapping
    public ApiResponse<?> createPost(@RequestBody PostRequest request) {
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.createPost(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/approve")
    public ApiResponse<?> approvePost(@RequestBody ApprovePostRequest request) {
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.approvePost(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/reject")
    public ApiResponse<?> rejectPost(@RequestBody RejectPostRequest request) {
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.rejectPost(request))
                .build();
    }

    // Get all users
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping("getAll")
    ApiResponse<ListPostResponse> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.<ListPostResponse>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPosts(page, size, sortBy, order, search))
                .build();
    }
}
