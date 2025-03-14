package com.learning_forum.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    private String token;
    private UserResponse user;
}
