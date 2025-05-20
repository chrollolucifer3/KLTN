package com.learning_forum.repository;

import com.learning_forum.entity.Post;
import com.learning_forum.entity.PostLike;
import com.learning_forum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
