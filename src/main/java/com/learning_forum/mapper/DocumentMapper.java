package com.learning_forum.mapper;

import com.learning_forum.dto.request.UploadDocumentRequest;
import com.learning_forum.dto.respone.DocumentFromCategoryResponse;
import com.learning_forum.dto.respone.DocumentResponse;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Document;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "fileName", source = "storedFileName")
    @Mapping(target = "fileUrl", expression = "java(\"/uploads/Document/\" + storedFileName)")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "user", expression = "java(mapUser(request.getUserId()))")
    @Mapping(target = "category", expression = "java(mapCategory(request.getCategoryId()))")
    Document toDocument(UploadDocumentRequest request, String storedFileName);

    default User mapUser(String userId) {
        if (userId == null) return null;
        User user = new User();
        user.setId(userId);
        return user;
    }

    default Category mapCategory(String categoryId) {
        if (categoryId == null) return null;
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }

    @Mappings(value = {
            @Mapping(target = "authorName", source = "user.fullName"),
            @Mapping(target = "categoryName", source = "category.name"),
    })
    DocumentResponse toDocumentResponseForAdmin(Document document);

    DocumentFromCategoryResponse toDocumentResponse(Document document);
}