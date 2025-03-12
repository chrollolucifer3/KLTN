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
public class UserResponseForAdmin {

    String id;
    String username;
    String email;
    String phone;
    String fullName;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    USER_ROLE role;

    Boolean isActive;
}
