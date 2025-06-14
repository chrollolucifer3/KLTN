package com.learning_forum.dto.respone;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListFivePostResponse {
    private List<PostResponse> posts;
}
