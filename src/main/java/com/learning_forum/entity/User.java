package com.learning_forum.entity;

import com.learning_forum.domain.USER_ROLE;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    String fullName;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    USER_ROLE role;

    Boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Column(name = "avatar_url")
    String avatarUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Post> posts;


    // Người dùng này đang theo dõi những ai
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Follow> following;

    // Những người đang theo dõi người dùng này
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Follow> followers;

    public int getFollowersCount() {
        return followers != null ? followers.size() : 0;
    }

    public int getFollowingCount() {
        return following != null ? following.size() : 0;
    }
}
