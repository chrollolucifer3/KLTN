package com.learning_forum.dto.respone;

import com.learning_forum.domain.STATUS;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    String id;
    String postId;
    String commentId;
    String userId;
    String reason;
    String createdAt;
    STATUS status;
    String authorName;
    String postTitle;
    String commentContent;
}
