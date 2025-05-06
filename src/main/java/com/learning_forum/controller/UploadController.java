package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.respone.UploadResponse;
import com.learning_forum.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/upload")
public class UploadController {

    FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.<UploadResponse>builder()
                        .code(400)
                        .message("File rỗng!")
                        .build();
            }

            String fileUrl = fileStorageService.storeFile(file);
            return ApiResponse.<UploadResponse>builder()
                    .code(200)
                    .message("Tải ảnh thành công")
                    .result(new UploadResponse(fileUrl))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ApiResponse.<UploadResponse>builder()
                    .code(500)
                    .message("Tải ảnh thất bại")
                    .build();
        }
    }
}
