package com.learning_forum.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String content;
    String postId;
    String userId;
    String createdAt;
    String updatedAt;
    String authorName;
}
