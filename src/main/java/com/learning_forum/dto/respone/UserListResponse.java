package com.learning_forum.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponse {
    private List<UserResponseForAdmin> users;
    private long total;
    private int totalPages;
    private int page;
    private int size;

}
