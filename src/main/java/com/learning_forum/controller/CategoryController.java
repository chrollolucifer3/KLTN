package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.CategoryParentResponse;
import com.learning_forum.dto.respone.CategoryResponse;
import com.learning_forum.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("category")

public class CategoryController {

    CategoryService categoryService;

    // Create category
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryCreateOrUpdateRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.createCategory(request))
                .build();
    }

    @PostMapping("/child")
    public ApiResponse<CategoryResponse> createCategoryChild(@RequestBody @Valid CategoryCreateOrUpdateRequest request) {
        System.out.println(request);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.createCategoryChild(request))
                .build();
    }

    // Get category by id
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable String id) {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getCategoryById(id))
                .build();
    }

    // Get all categories
    @GetMapping
    public ApiResponse<List<CategoryParentResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryParentResponse>>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getAllParentCategories())
                .build();
    }

    // Get all subcategories
    @GetMapping("/sub")
    public ApiResponse<List<CategoryParentResponse>> getAllSubCategories() {
        return ApiResponse.<List<CategoryParentResponse>>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getAllSubCategories())
                .build();
    }

}
