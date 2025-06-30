package com.learning_forum.dto.request;

import com.learning_forum.domain.NOTIFICATION_TYPE;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationMessage {
    @Enumerated(EnumType.STRING)
    NOTIFICATION_TYPE type;
    String title;
    String content;
    String postId;
    String authorName;
    LocalDateTime createdAt;
}
