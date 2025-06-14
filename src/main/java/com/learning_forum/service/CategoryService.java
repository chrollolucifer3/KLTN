package com.learning_forum.service;

import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.CategoryParentResponse;
import com.learning_forum.dto.respone.CategoryResponse;
import com.learning_forum.dto.respone.HomeClientResponse;
import com.learning_forum.dto.respone.ListCategoryResponse;
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
import java.util.stream.Collectors;

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

    // Get all categories and sub categories and their posts
    public List<HomeClientResponse> getAllCategoriesAndSubCategories() {
        log.info("Get all categories and sub categories and their posts");

        // Lấy tất cả danh mục cha (parentCategory == null)
        List<Category> parentCategories = categoryRepository.findByParentCategoryIsNull();

        // Dùng mapper để đệ quy lấy tất cả subCategory và bài viết
        return parentCategories.stream()
                .map(categoryMapper::toHomeClientResponseRecursive)
                .collect(Collectors.toList());
    }

    // Get all categories
    public List<ListCategoryResponse> getAllCategories() {
        log.info("Get all categories");
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(categoryMapper::toListCategoryResponse)
                .collect(Collectors.toList());
    }

    // update category
    public void updateCategory(String id, CategoryCreateOrUpdateRequest request) {
        log.info("Update category with id: {} and request: {}", id, request);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Cập nhật thông tin từ request
        category.setName(request.getName());

        // Nếu có parentId thì cập nhật parentCategory
        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null); // nếu không có parentId thì đặt là null
        }
        // Lưu lại category đã cập nhật
        categoryRepository.save(category);
    }
}
