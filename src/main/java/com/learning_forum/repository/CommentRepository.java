package com.learning_forum.repository;

import com.learning_forum.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, String>, JpaSpecificationExecutor<Comment> {
    Page<Comment> findByPostId(String postId, Pageable pageable);
    Optional<Comment> findCommentById(String id);
    @Query("SELECT COALESCE(COUNT(c), 0) FROM Comment c WHERE c.createdAt >= :start AND c.createdAt < :end")
    int countCommentsInCurrentMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
