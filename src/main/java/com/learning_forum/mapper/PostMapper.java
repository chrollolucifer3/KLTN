package com.learning_forum.mapper;


import com.learning_forum.dto.request.PostRequest;
import com.learning_forum.dto.respone.PostResponse;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "user", expression = "java(mapUser(request.getUserId()))")
    @Mapping(target = "category", expression = "java(mapCategory(request.getCategoryId()))")
    Post toPost(PostRequest request);

    default User mapUser(String userId) {
        if (userId == null) return null;
        User user = new User();
        user.setId((userId));
        return user;
    }

    default Category mapCategory(String categoryId) {
        if (categoryId == null) return null;
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }

    @Mappings(value = {
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "categoryId", source = "category.id"),
            @Mapping(target = "authorName", source = "user.fullName"),
    })
    PostResponse toPostResponse(Post post);
}
