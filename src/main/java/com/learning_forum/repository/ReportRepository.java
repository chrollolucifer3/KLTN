package com.learning_forum.repository;

import com.learning_forum.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, String>, JpaSpecificationExecutor<Report> {
    void deleteByCommentId(String commentId);
    @Query("SELECT r FROM Report r WHERE r.id = :id")
    Optional<Report> findReportById(@Param("id") String id);
    List<Report> findByCommentId(String commentId);
}
