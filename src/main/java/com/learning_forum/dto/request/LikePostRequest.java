package com.learning_forum.dto.request;

import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LikePostRequest {
    private String userId;
    private String postId;
}
