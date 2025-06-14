package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.ApproveOrRejectPostRequest;
import com.learning_forum.dto.request.UploadDocumentRequest;
import com.learning_forum.dto.respone.ListDocumentResponse;
import com.learning_forum.dto.respone.ListDocumentResponseForAdmin;
import com.learning_forum.dto.respone.UploadResponse;
import com.learning_forum.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("document")
public class DocumentController {

    FileStorageService fileStorageService;

    @PostMapping
    public ApiResponse<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") String categoryId,
            @RequestParam("userId") String userId,
            @RequestParam("fileName") String fileName // có thể là tiêu đề hoặc tên hiển thị
    ) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.<UploadResponse>builder()
                        .code(400)
                        .message("File rỗng!")
                        .build();
            }

            UploadDocumentRequest request = UploadDocumentRequest.builder()
                    .categoryId(categoryId)
                    .userId(userId)
                    .fileName(fileName)
                    .build();

            String fileUrl = fileStorageService.storeDocument(file, request);

            return ApiResponse.<UploadResponse>builder()
                    .code(200)
                    .message("Tải tài liệu thành công")
                    .result(new UploadResponse(fileUrl))
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi upload file", e);
            return ApiResponse.<UploadResponse>builder()
                    .code(500)
                    .message("Tải tài liệu thất bại")
                    .build();
        }
    }

    @GetMapping("/getAll")
    public ApiResponse<ListDocumentResponseForAdmin> getAllDocument(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.<ListDocumentResponseForAdmin>builder()
                .code(200)
                .message("Success")
                .result(fileStorageService.getAllDocuments(page, size, sortBy, order, search))
                .build();
    }

    @GetMapping("/getByCategory/{id}")
    public ApiResponse<ListDocumentResponse> getDocumentsByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String order
    ) {
        return ApiResponse.<ListDocumentResponse>builder()
                .code(200)
                .message("Success")
                .result(fileStorageService.getDocumentsByCategory(id, page, size, sortBy, order))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("approveOrReject")
    public ApiResponse<?> approveOrRejectDocument(@RequestBody ApproveOrRejectPostRequest request) {
        log.info("DocumentController.ApproveDocument with: {}", request);
        fileStorageService.approveOrRejectDocument(request);
        return ApiResponse.builder()
                .code(200)
                .message("Success")
                .build();
    }

    // Lấy số lượng được tải lên trong tháng hiện tại
    @GetMapping("/count")
    public ApiResponse<Integer> countDocumentsInCurrentMonth() {
        log.info("DocumentController.countDocumentsInCurrentMonth");
        int count = fileStorageService.countDocumentsInCurrentMonth();
        return ApiResponse.<Integer>builder()
                .code(200)
                .message("Success")
                .result(count)
                .build();
    }
}
