package com.learning_forum.dto.respone;

import com.learning_forum.domain.USER_ROLE;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String id;
    String username;
    String email;
    String phone;
    String fullName;
    LocalDate dob;
    LocalDate createdDate;
}
