package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/upload")
public class UploadController {

    FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.builder()
                        .code(400)
                        .message("File rỗng!")
                        .build();
            }

            String fileUrl = fileStorageService.storeFile(file);
            return ApiResponse.builder()
                    .code(200)
                    .message("Tải ảnh thành công")
                    .result(new UploadResponse(fileUrl))
                    .build();
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để debug dễ hơn
            return ApiResponse.builder()
                    .code(500)
                    .message("Tải ảnh thất bại")
                    .build();
        }
    }

    static class UploadResponse {
        public String url;

        public UploadResponse(String url) {
            this.url = url;
        }
    }
}
