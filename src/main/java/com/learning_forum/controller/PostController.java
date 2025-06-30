package com.learning_forum.controller;

import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.*;
import com.learning_forum.entity.Post;
import com.learning_forum.service.NotificationService;
import com.learning_forum.service.PostLikeService;
import com.learning_forum.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("post")
public class PostController {

    PostService postService;
    PostLikeService postLikeService;
//    NotificationService notificationService;

    @PostMapping
    public ApiResponse<PostResponse> createPost(@RequestBody PostRequest request) {
        log.info("PostController.CreatePost with request: {}", request);

        // 1. Tạo bài viết
        PostResponse postResponse = postService.createPost(request);

        return ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Success")
                .result(postResponse)
                .build();
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/approve")
    public ApiResponse<PostResponse> approvePost(@RequestBody ApprovePostRequest request) {
        log.info("PostController.ApprovePost with request: {}", request);
        return ApiResponse.<PostResponse>builder()
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
    @GetMapping("/getAll")
    ApiResponse<ListPostResponseForAdmin> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        log.info("PostController.GetAllPost with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPosts(page, size, sortBy, order, search, status))
                .build();
    }

    // Get all posts For Client
    @GetMapping("/getAllForClient")
    ApiResponse<ListPostResponseForAdmin> getAllPostForClient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false) String search
    ) {
        log.info("PostController.GetAllPostForClient with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPostsForClient(page, size, sortBy, order, search))
                .build();
    }

    // Get all posts by category
    @GetMapping("/category/{id}")
    ApiResponse <ListPostResponse> getAllPostByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
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

    @GetMapping("/get/top-liked-month")
    ApiResponse<ListFivePostResponse> getFivePostMostLikedInMonth() {
        log.info("PostController.GetFivePostMostLikedInMonth");
        return ApiResponse.<ListFivePostResponse>builder()
                .code(200)
                .message("Success")
                .result(postService.getFivePostMostLiked())
                .build();
    }

    //Lấy số lượng bài viết mới trong tháng hiện tại
    @GetMapping("/get/count-posts-this-month")
    ApiResponse<Integer> getCountPostsThisMonth() {
        log.info("PostController.GetCountPostsThisMonth");
        return ApiResponse.<Integer>builder()
                .code(200)
                .message("Success")
                .result(postService.getCountPostsThisMonth())
                .build();
    }

    //Lấy số lượng bài viết mới theo khoảng thời gian truyền về
    @PostMapping("/get/count-posts-by-time")
    ApiResponse<List<CountPostsByTimeResponse>> getCountPostsByTime(@RequestBody TimeRangeRequest request) {
        log.info("PostController.GetCountPostsByTime with request: {}", request);
        return ApiResponse.<List<CountPostsByTimeResponse>>builder()
                .code(200)
                .message("Success")
                .result(postService.getCountPostsByTime(request))
                .build();
    }

    // Get all posts by user
    @GetMapping("/user")
    ApiResponse<ListPostResponseForAdmin> getAllPostByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false)String status
    ) {
        log.info("PostController.GetAllPostByUser with: page: {}, size: {}, sortBy: {}, order: {}, status: {}", page, size, sortBy, order, status);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getAllPostsByUser(page, size, sortBy, order, status))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/delete/{id}")
    public ApiResponse<?> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        log.info("PostController.DeletePost with id: {}", id);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    //get post by user_id
    @GetMapping("/user/{userId}")
    ApiResponse<ListPostResponseForAdmin> getPostByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false) STATUS status
    ) {
        log.info("PostController.GetPostByUserId with userId: {}, page: {}, size: {}, sortBy: {}, order: {}, status: {}", userId, page, size, sortBy, order, status);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getPostsByUserId(userId, page, size, sortBy, order, status))
                .build();
    }

    // Cập nhật bài viết
    @PostMapping("/update")
    public ApiResponse<?> updatePost( @RequestBody UpdatePostRequest request) {
        postService.updatePost(request);
        return ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Lấy bài viết đã thích của người dùng
    @GetMapping("/liked")
    public ApiResponse<ListPostResponseForAdmin> getLikedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order,
            @RequestParam(required = false) String search
    ) {
        log.info("PostController.GetLikedPosts with page: {}, size: {}, sortBy: {}, order: {}", page, size, sortBy, order);
        return ApiResponse.<ListPostResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(postService.getLikedPosts(page, size, sortBy, order, search))
                .build();
    }
}
