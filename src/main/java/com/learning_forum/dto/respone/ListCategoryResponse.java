package com.learning_forum.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCategoryResponse {
    String id;
    String name;
    CategoryParentResponse parentCategory;
}
