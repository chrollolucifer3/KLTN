package com.learning_forum.repository;

import com.learning_forum.domain.STATUS;
import com.learning_forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    Page<Post> findAllByCategoryIdAndStatus(String categoryId, STATUS status, Pageable pageable);
    Page<Post> findAllByUserId(String userId, Pageable pageable);
    //JPA tự convert enum STATUS sang String
    List<Post> findTop5ByStatusOrderByCreatedAtDesc(STATUS status);
    Optional<Post> findPostById(String postId);
    List<Post> findAllByUserId(String userId);
    @Query(value = """
        SELECT *
        FROM posts
        WHERE status = :status
        ORDER BY likes_count DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Post> findTop10PostByLikesCount(@Param("status") String status);
    // Khi sử dụng Query phải để kiểu status là String vì JPA không hỗ trợ enum trong native query
    @Query("SELECT COALESCE(COUNT(p), 0) FROM Post p WHERE p.createdAt >= :start AND p.createdAt < :end AND p.status = :status")
    int countByCreatedAtAfterAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("status") STATUS status);

    @Query("SELECT MONTH(p.createdAt) AS month, COUNT(p) AS totalPosts " +
            "FROM Post p WHERE p.createdAt BETWEEN :startTime AND :endTime " +
            "GROUP BY MONTH(p.createdAt)")
    List<Object[]> countPostsByTimeRange(@Param("startTime") LocalDateTime startDate, @Param("endTime") LocalDateTime endDate);

    Page<Post> findAllByStatus(STATUS status, Pageable pageable);
    Page<Post> findAllByUserIdAndStatus(String userId, STATUS status, Pageable pageable);
    Page<Post> findByStatusAndTitleContainingIgnoreCase(STATUS status, String title, Pageable pageable);

    // PostRepository.java
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.status = 'APPROVED'")
    Long countApprovedPostsByUserId(@Param("userId") String userId);

}
