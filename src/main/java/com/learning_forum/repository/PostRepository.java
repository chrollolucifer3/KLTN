package com.learning_forum.repository;

import com.learning_forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    Page<Post> findAllByCategoryId(String categoryId, Pageable pageable);
    List<Post> findTop5ByOrderByCreatedAtDesc();
}
