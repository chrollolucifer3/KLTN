package com.learning_forum.controller;

import com.learning_forum.dto.request.ApiResponse;
import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.CategoryParentResponse;
import com.learning_forum.dto.respone.CategoryResponse;
import com.learning_forum.dto.respone.HomeClientResponse;
import com.learning_forum.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("category")

public class CategoryController {

    CategoryService categoryService;

    // Create category
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryCreateOrUpdateRequest request) {
        log.info("CategoryController.CreateCategory with request: {}", request);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.createCategory(request))
                .build();
    }

    @PostMapping("/child")
    public ApiResponse<CategoryResponse> createCategoryChild(@RequestBody @Valid CategoryCreateOrUpdateRequest request) {
        log.info("CategoryController.CreateCategoryChild with request: {}", request);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.createCategoryChild(request))
                .build();
    }

    // Get category by id
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable String id) {
        log.info("CategoryController.GetCategoryById with id: {}", id);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getCategoryById(id))
                .build();
    }

    // Get all categories
    @GetMapping
    public ApiResponse<List<CategoryParentResponse>> getAllCategories() {
        log.info("CategoryController.GetAllCategories");
        return ApiResponse.<List<CategoryParentResponse>>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getAllParentCategories())
                .build();
    }

    // Get all subcategories
    @GetMapping("/sub")
    public ApiResponse<List<CategoryParentResponse>> getAllSubCategories() {
        log.info("CategoryController.GetAllSubCategories");
        return ApiResponse.<List<CategoryParentResponse>>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getAllSubCategories())
                .build();
    }

    // Get all categories and subcategories and their posts
    @GetMapping("/all")
    public ApiResponse<List<HomeClientResponse>> getAllCategoriesAndSubCategories() {
        log.info("CategoryController.GetAllCategoriesAndSubCategories");
        return ApiResponse.<List<HomeClientResponse>>builder()
                .code(200)
                .message("Success")
                .result(categoryService.getAllCategoriesAndSubCategories())
                .build();
    }
}
