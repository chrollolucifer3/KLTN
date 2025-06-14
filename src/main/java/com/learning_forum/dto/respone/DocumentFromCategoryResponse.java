package com.learning_forum.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentFromCategoryResponse {
    String id;
    String fileName;
    String fileUrl;
}
