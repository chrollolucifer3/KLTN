package com.learning_forum.dto.respone;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListCommentResponse {
    private List<CommentResponse> comments;
    private long total;
    private int totalPages;
    private int page;
    private int size;
}
