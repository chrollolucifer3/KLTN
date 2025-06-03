package com.learning_forum.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostOrCommentResponse {
    private String id;
    private String type; // "POST" hoặc "COMMENT"
    private String title; // Chỉ có nếu là bài viết
    private String content;
    private String authorName;
}
