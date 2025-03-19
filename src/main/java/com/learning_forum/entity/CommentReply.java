package com.learning_forum.entity;

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
@Table(name = "comment_replies")
public class CommentReply {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;


    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    Comment comment; // Bình luận gốc

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user; // Người phản hồi

    @Column(nullable = false, columnDefinition = "TEXT")
    String content; // Nội dung phản hồi

    LocalDateTime createdAt = LocalDateTime.now();
    LocalDateTime updatedAt = LocalDateTime.now();

}
