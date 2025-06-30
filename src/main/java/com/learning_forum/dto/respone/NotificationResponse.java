package com.learning_forum.dto.respone;

import com.learning_forum.domain.NOTIFICATION_TYPE;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    NOTIFICATION_TYPE type;
    String title;
    String content;
    String postId;
    String authorName;
    LocalDateTime createdAt;
    Boolean isRead;
}
