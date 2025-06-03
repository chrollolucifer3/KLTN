package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.GetPostOrComment;
import com.learning_forum.dto.request.ReportRequest;
import com.learning_forum.dto.respone.ListReportResponse;
import com.learning_forum.dto.respone.PostOrCommentResponse;
import com.learning_forum.service.ReportService;
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
@RequestMapping("report")
public class ReportController {

    ReportService reportService;

    @PostMapping
    public ApiResponse<?> createReport(@RequestBody ReportRequest request) {
        log.info("ReportController.createReport with request: {}", request);
        reportService.createReport(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    //get all report
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping("getAll")
    ApiResponse<ListReportResponse> getAllReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String search
    ) {
        log.info("PostController.GetAllPost with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        return ApiResponse.<ListReportResponse>builder()
                .code(200)
                .message("Success")
                .result(reportService.getAll(page, size, sortBy, order, search))
                .build();
    }

    //get post or comment by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("getById")
    ApiResponse<PostOrCommentResponse> getPostOrCommentById(@RequestBody GetPostOrComment request) {
        log.info("ReportController.getPostOrCommentById with: {}", request);
        return ApiResponse.<PostOrCommentResponse>builder()
                .code(200)
                .message("Success")
                .result(reportService.getPostOrCommentById(request))
                .build();
    }

    //delete comment or post by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("delete")
    ApiResponse<?> deletePostOrCommentById(@RequestBody GetPostOrComment request) {
        log.info("ReportController.deletePostOrCommentById with: {}", request);
        reportService.deletePostOrCommentById(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }
}
