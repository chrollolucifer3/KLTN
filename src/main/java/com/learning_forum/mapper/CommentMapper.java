package com.learning_forum.mapper;

import com.learning_forum.dto.request.CommentRequest;
import com.learning_forum.dto.respone.CommentResponse;
import com.learning_forum.dto.respone.ListCommentResponse;
import com.learning_forum.entity.Comment;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "user", expression = "java(mapUser(request.getUserId()))")
    @Mapping(target = "post", expression = "java(mapPost(request.getPostId()))")
    Comment toComment(CommentRequest request);

    default User mapUser(String userId) {
        if (userId == null) return null;
        User user = new User();
        user.setId((userId));
        return user;
    }

    default Post mapPost(String PostId) {
        if (PostId == null) return null;
        Post post = new Post();
        post.setId(PostId);
        return post;
    }

    @Mappings(value = {
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "authorName", source = "user.fullName"),
            @Mapping(target = "postId", source = "post.id"),
    })
    CommentResponse toCommentResponse(Comment comment);
}
