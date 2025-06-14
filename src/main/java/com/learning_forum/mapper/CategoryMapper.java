package com.learning_forum.mapper;

import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.*;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreateOrUpdateRequest request);

    default CategoryResponse toCategoryResponse(Category category) {
        // Map sub-categories (đệ quy)
        List<CategoryResponse> subCategoryResponses = category.getSubCategories() != null
                ? category.getSubCategories().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        // Map posts từ Post → PostFromCategoryResponse
        Set<PostFromCategoryResponse> postResponses = category.getPosts() != null
                ? category.getPosts().stream()
                .map(this::toPostFromCategoryResponse)
                .collect(Collectors.toSet())
                : new HashSet<>();

        // Trả về DTO
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .posts(postResponses)
                .subCategories(subCategoryResponses)
                .build();
    }

//    HomeClientResponse toHomeClientResponse(Category category);

    // Đệ quy để trả về HomeClientResponse bao gồm bài viết và các category con
    default HomeClientResponse toHomeClientResponseRecursive(Category category) {
        List<HomeClientResponse> subCategoryResponses = category.getSubCategories() != null
                ? category.getSubCategories().stream()
                .map(this::toHomeClientResponseRecursive)
                .collect(Collectors.toList())
                : List.of();

        Set<PostFromCategoryResponse> postResponses = category.getPosts() != null
                ? category.getPosts().stream()
                .map(this::toPostFromCategoryResponse)
                .collect(Collectors.toSet())
                : Set.of();

        return HomeClientResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .posts(postResponses)
                .subCategories(subCategoryResponses)
                .build();
    }

//    CategoryParentResponse toCategoryParentResponse(Category category);
    PostFromCategoryResponse toPostFromCategoryResponse(Post post);

    /**
     * Map từ Category → ListCategoryResponse (2 mức, có parentCategory)
     */
    @Mapping(target = "parentCategory", source = "parentCategory", qualifiedByName = "toCategoryParentResponse")
    ListCategoryResponse toListCategoryResponse(Category category);

    /**
     * Map Category → CategoryParentResponse (id + name)
     */
    @Named("toCategoryParentResponse")
    default CategoryParentResponse toCategoryParentResponse(Category category) {
        if (category == null) return null;
        return CategoryParentResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

}
