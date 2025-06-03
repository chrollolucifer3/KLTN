package com.learning_forum.entity;

import com.learning_forum.domain.STATUS;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user; // Người báo cáo

    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post; // Báo cáo bài viết hoặc bình luận

    @ManyToOne
    @JoinColumn(name = "comment_id")
    Comment comment; // Báo cáo bài viết hoặc bình luận

    @Column(nullable = false)
    String reason; // Lý do báo cáo

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    STATUS status = STATUS.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
