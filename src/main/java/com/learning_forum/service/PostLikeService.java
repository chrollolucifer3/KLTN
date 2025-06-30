package com.learning_forum.service;

import com.learning_forum.dto.request.LikePostRequest;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.PostLike;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.repository.PostLikeRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostLikeService {

    PostLikeRepository postLikeRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    NotificationService notificationService;

    @Transactional
    public boolean toggleLikePost(LikePostRequest request) {
        log.info("Like post with {}", request);
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(request.getPostId()).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        var postLike = postLikeRepository.findByUserAndPost(user, post);

        if (postLike.isPresent()) {
            postLikeRepository.delete(postLike.get());
            post.setLikesCount(post.getLikesCount() - 1);
            postRepository.save(post);
            return false;
        } else {
            postLikeRepository.save(PostLike.builder().user(user).post(post).build());
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
            // Nếu người dùng thích bài viết không phải là tác giả của bài viết, gửi thông báo
            if (!Objects.equals(user.getId(), post.getUser().getId())) {
                notificationService.notifyAuthorPostLiked(post.getId(), user.getId());
            }
            return true;
        }
    }
}
