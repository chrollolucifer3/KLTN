package com.learning_forum.repository;

import com.learning_forum.domain.STATUS;
import com.learning_forum.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {
    Page<Document> findAllByCategoryIdAndStatus(String id, STATUS status, Pageable pageable);
    @Query("SELECT COALESCE(COUNT(d), 0) FROM Document d WHERE d.createdAt >= :start AND d.createdAt < :end AND d.status = :status")
    int countByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") STATUS status
    );

    @Query("""
    SELECT d FROM Document d
    WHERE d.user.id = :userId
      AND d.status = :status
      AND (:search IS NULL OR LOWER(d.fileName) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<Document> findByUserAndStatusAndSearch(
            @Param("userId") String userId,
            @Param("status") STATUS status,
            @Param("search") String search,
            Pageable pageable
    );
}
