package com.learning_forum.mapper;

import com.learning_forum.dto.request.ReportRequest;
import com.learning_forum.dto.respone.PostOrCommentResponse;
import com.learning_forum.dto.respone.PostResponse;
import com.learning_forum.dto.respone.ReportResponse;
import com.learning_forum.entity.Comment;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.Report;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "user", expression = "java(mapUser(request.getUserId()))")
    @Mapping(target = "post", expression = "java(mapPost(request.getPostId()))")
    @Mapping(target = "comment", expression = "java(mapComment(request.getCommentId()))")
    Report toReport(ReportRequest request);

    default User mapUser(String userId) {
        if (userId == null) return null;
        User user = new User();
        user.setId(userId);
        return user;
    }

    default Post mapPost(String postId) {
        if (postId == null) return null;
        Post post = new Post();
        post.setId(postId);
        return post;
    }

    default Comment mapComment(String commentId) {
        if (commentId == null) return null;
        Comment comment = new Comment();
        comment.setId(commentId);
        return comment;
    }

    @Mappings(value = {
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "postId", source = "post.id"),
            @Mapping(target = "commentId", source = "comment.id"),
            @Mapping(target = "authorName", source = "user.fullName"),
            @Mapping(target = "postTitle", source = "post.title"),
            @Mapping(target = "commentContent", source = "comment.content")
    })
    ReportResponse toReportForAdmin(Report report);

    // Post -> PostOrCommentResponse
    default PostOrCommentResponse toPostOrCommentResponse(Post post) {
        if (post == null) return null;
        PostOrCommentResponse response = new PostOrCommentResponse();
        response.setId(post.getId());
        response.setType("POST");
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorName(post.getUser() != null ? post.getUser().getFullName() : null);
        return response;
    }

    // Comment -> PostOrCommentResponse
    default PostOrCommentResponse toPostOrCommentResponse(Comment comment) {
        if (comment == null) return null;
        PostOrCommentResponse response = new PostOrCommentResponse();
        response.setId(comment.getId());
        response.setType("COMMENT");
        response.setContent(comment.getContent());
        response.setAuthorName(comment.getUser() != null ? comment.getUser().getFullName() : null);
        return response;
    }
}
