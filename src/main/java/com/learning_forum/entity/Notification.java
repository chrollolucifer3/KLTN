package com.learning_forum.entity;
import com.learning_forum.domain.NOTIFICATION_TYPE;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    NOTIFICATION_TYPE type;

    String title;
    String content;
    String postId;
    String authorName;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    User recipient; // Người nhận thông báo
}
