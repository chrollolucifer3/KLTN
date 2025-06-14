package com.learning_forum.dto.respone;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListDocumentResponseForAdmin {
    private List<DocumentResponse> documents;
    private long total;
    private int totalPages;
    private int page;
    private int size;
}
