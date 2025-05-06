package com.learning_forum.mapper;

import com.learning_forum.dto.request.CategoryCreateOrUpdateRequest;
import com.learning_forum.dto.respone.CategoryParentResponse;
import com.learning_forum.dto.respone.CategoryResponse;
import com.learning_forum.entity.Category;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreateOrUpdateRequest request);

    default CategoryResponse toCategoryResponse(Category category) {
        List<CategoryResponse> subCategoryResponses = category.getSubCategories() != null
                ? category.getSubCategories().stream()
                .map(this::toCategoryResponse) // đệ quy an toàn
                .collect(Collectors.toList())
                : new ArrayList<>();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subCategories(subCategoryResponses)
                .build();
    }

    CategoryParentResponse toCategoryParentResponse(Category category);
}
