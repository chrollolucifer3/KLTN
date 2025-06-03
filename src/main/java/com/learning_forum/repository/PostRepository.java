package com.learning_forum.repository;

import com.learning_forum.domain.STATUS;
import com.learning_forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    Page<Post> findAllByCategoryIdAndStatus(String categoryId, STATUS status, Pageable pageable);
    List<Post> findTop5ByStatusOrderByCreatedAtDesc(STATUS status);
    Optional<Post> findPostById(String postId);
    List<Post> findAllByUserId(String userId);
}
