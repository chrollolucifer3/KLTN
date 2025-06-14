package com.learning_forum.dto.request;

import com.learning_forum.entity.Post;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryCreateOrUpdateRequest {
    @NotBlank(message = "tên danh mục không được để trống!")
    String name;
    @JoinColumn(name = "parent_id")
    String parentId;
}

