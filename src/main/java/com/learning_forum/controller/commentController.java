package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.CommentRequest;
import com.learning_forum.dto.request.DeleteCommentRequest;
import com.learning_forum.dto.request.UpdateCommentRequest;
import com.learning_forum.dto.respone.CommentResponse;
import com.learning_forum.dto.respone.ListCommentResponse;
import com.learning_forum.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("comment")
public class commentController {
    CommentService commentService;

    // Create comment
    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody CommentRequest request) {
        log.info("CommentController.CreateComment with request: {}", request);
        return ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Success")
                .result(commentService.createComment(request))
                .build();
    }

    // Get list comment by post id
    @GetMapping("/getList/{id}")
    public ApiResponse<ListCommentResponse> getListCommentByPostId(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order) {

        log.info("CommentController.GetListCommentByPostId with id: {}", id);

        return ApiResponse.<ListCommentResponse>builder()
                .code(200)
                .message("Success")
                .result(commentService.getListCommentByPostId(id, page, size, sortBy, order))
                .build();
    }

    // Delete comment
    @PostMapping("/delete")
    public ApiResponse<?> deleteComment(@RequestBody DeleteCommentRequest request) {
        log.info("CommentController.DeleteComment with: {}", request);
        commentService.deleteComment(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Update comment
    @PostMapping("/update/{id}")
    public ApiResponse<?> updateComment(@PathVariable String id, @RequestBody UpdateCommentRequest request) {
        log.info("CommentController.UpdateComment with id: {}, request: {}", id, request);
        commentService.updateComment(id, request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    //Lấy số lượng comment trong tháng hiện tại
    @GetMapping("/count")
    public ApiResponse<Integer> countCommentsInCurrentMonth() {
        log.info("CommentController.countCommentsInCurrentMonth");
        int count = commentService.countCommentsInCurrentMonth();
        return ApiResponse.<Integer>builder()
                .code(200)
                .message("Success")
                .result(count)
                .build();
    }
}
