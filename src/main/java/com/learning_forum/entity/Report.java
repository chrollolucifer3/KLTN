package com.learning_forum.entity;

import com.learning_forum.domain.STATUS;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @JoinColumn(name = "reporter_id", nullable = false)
    User reporter; // Người báo cáo

    @ManyToOne
    @JoinColumn(name = "reported_post_id")
    Post reportedPost; // Báo cáo bài viết hoặc bình luận

    @ManyToOne
    @JoinColumn(name = "reported_comment_id")
    Comment reportedComment; // Báo cáo bài viết hoặc bình luận

    @Column(nullable = false, columnDefinition = "TEXT")
    String reason; // Lý do báo cáo

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    STATUS status = STATUS.PENDING;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}
