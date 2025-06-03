package com.learning_forum.repository;

import com.learning_forum.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<Report, String>, JpaSpecificationExecutor<Report> {
}
