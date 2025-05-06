package com.learning_forum.dto.request;

import com.learning_forum.domain.STATUS;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovePostRequest {
    String id;
    String categoryId;
    STATUS status = STATUS.APPROVED;
}
