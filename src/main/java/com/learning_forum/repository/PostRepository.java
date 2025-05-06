package com.learning_forum.repository;

import com.learning_forum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {

}
