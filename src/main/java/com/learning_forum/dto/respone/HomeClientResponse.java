package com.learning_forum.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeClientResponse {
    String id;
    String name;
    Set<PostFromCategoryResponse> posts;
    List<HomeClientResponse> subCategories;
}
