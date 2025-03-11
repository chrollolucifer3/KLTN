package com.learning_forum.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private UserResponse user;
}
