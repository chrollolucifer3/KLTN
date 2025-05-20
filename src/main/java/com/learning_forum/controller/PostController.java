package com.learning_forum.controller;

import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.ListPostResponse;
import com.learning_forum.dto.respone.ListPostResponseForAdmin;
import com.learning_forum.service.PostLikeService;
import com.learning_forum.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("post")
public class PostController {

    PostService postService;
    PostLikeService postLikeService;

    @PostMapping
    public ApiResponse<?> createPost(@RequestBody PostRequest request) {
        log.info("PostController.CreatePost with request: {}", request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.createPost(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/approve")
    public ApiResponse<?> approvePost(@RequestBody ApprovePostRequest request) {
        log.info("PostController.ApprovePost with request: {}", request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.approvePost(request))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/reject")
    public ApiResponse<?> rejectPost(@RequestBody RejectPostRequest request) {
        log.info("PostController.RejectPost with request: {}", request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.rejectPost(request))
                .build();
    }

    // Get all posts
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping("getAll")
    ApiResponse<ListPostResponseForAdmin> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String search
    ) {
        log.info("PostController.GetAllPost with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPosts(page, size, sortBy, order, search))
                .build();
    }

    // Get all posts by category
    @GetMapping("/category/{id}")
    ApiResponse <ListPostResponse> getAllPostByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String search
    ) {
        log.info("PostController.GetAllPostByCategory with id: {}, page: {}, size: {}, sortBy: {}, order: {}, search: {}", id, page, size, sortBy, order, search);
        return ApiResponse.<ListPostResponse>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPostsByCategory(id, page, size, sortBy, order))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<?> getPostById(@PathVariable String id) {
        log.info("PostController.GetPostById with id: {}", id);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.getPostById(id))
                .build();
    }

    @PostMapping("like")
    ApiResponse<?> toggleLikePost(@RequestBody LikePostRequest request) {
        log.info("PostController.ToggleLikePost with request: {}", request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postLikeService.toggleLikePost(request))
                .build();
    }

    // Lấy 5 bài viết mới nhất
    @GetMapping("getPost")
    ApiResponse<?> getPost() {
        log.info("PostController.GetPost");
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .result(postService.getPost())
                .build();
    }
}
