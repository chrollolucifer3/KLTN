package com.learning_forum.dto.respone;

import com.learning_forum.domain.STATUS;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String title;
    String content;
    String categoryId;
    String userId;
    STATUS status;
    String rejectReason;
    String createdAt;
    String updatedAt;
    String authorName;
    String categoryName;
    boolean isLiked;
    int likeCount;
}
