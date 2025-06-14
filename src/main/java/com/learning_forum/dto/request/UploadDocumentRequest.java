package com.learning_forum.dto.request;

import com.learning_forum.domain.STATUS;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadDocumentRequest {
    String categoryId;
    String userId;
    STATUS status;
    String fileName;
}
