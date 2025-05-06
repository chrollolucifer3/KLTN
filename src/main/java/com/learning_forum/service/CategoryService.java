package com.learning_forum.service;

import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.CategoryParentResponse;
import com.learning_forum.dto.respone.CategoryResponse;
import com.learning_forum.entity.Category;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.CategoryMapper;
import com.learning_forum.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    // Create category
    public CategoryResponse createCategory(CategoryCreateOrUpdateRequest request) {
        log.info("Create Category with request: {}", request);
        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    // Create Category child
    public CategoryResponse createCategoryChild(CategoryCreateOrUpdateRequest request) {
        log.info("Create Category child with request: {}", request);
        Category parentCategory = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Category category = categoryMapper.toCategory(request);
        category.setParentCategory(parentCategory);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    //Get category by id
    public CategoryResponse getCategoryById(String id) {
        log.info("Get Category by id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryParentResponse> getAllParentCategories() {
        log.info("Get all parent categories");
        List<Category> parentCategories = categoryRepository.findByParentCategoryIsNull();
        return parentCategories.stream()
                .map(categoryMapper::toCategoryParentResponse)
                .toList();
    }

    //Get all sub categories
    public List<CategoryParentResponse> getAllSubCategories() {
        log.info("Get all sub categories");
        List<Category> subCategories = categoryRepository.findByParentCategoryIsNotNull();
        return subCategories.stream()
                .map(categoryMapper::toCategoryParentResponse)
                .toList();
    }
}
