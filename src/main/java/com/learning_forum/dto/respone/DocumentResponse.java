package com.learning_forum.dto.respone;

import com.learning_forum.domain.STATUS;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentResponse {
    String id;
    String fileName;
    STATUS status;
    String createdAt;
    String fileUrl;
    String rejectReason;
    String authorName;
    String categoryName;
}
