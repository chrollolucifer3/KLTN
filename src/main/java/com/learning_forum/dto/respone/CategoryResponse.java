package com.learning_forum.dto.respone;

import com.learning_forum.entity.Post;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String name;
    String parentId; // chỉ giữ id, không giữ object cha
    Set<Post> posts;
    List<CategoryResponse> subCategories;
}
