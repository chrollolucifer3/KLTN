package com.learning_forum.dto.respone;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListPostResponse {
    private String categoryName;
    private List<PostFromCategoryResponse> posts;
    private long total;
    private int totalPages;
    private int page;
    private int size;
}
