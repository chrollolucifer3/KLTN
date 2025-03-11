package com.learning_forum.entity;

import com.learning_forum.domain.USER_ROLE;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(nullable = false, unique = true)
    String username;
    String password;
    String email;
    String phone;
    @Enumerated(EnumType.STRING)
    USER_ROLE role = USER_ROLE.USER;
    String fullName;
    LocalDate dob;
}
