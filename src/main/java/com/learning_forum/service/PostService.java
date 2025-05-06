package com.learning_forum.service;

import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.ApprovePostRequest;
import com.learning_forum.dto.request.PostRequest;
import com.learning_forum.dto.request.RejectPostRequest;
import com.learning_forum.dto.respone.ListPostResponse;
import com.learning_forum.dto.respone.PostResponse;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Post;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.PostMapper;
import com.learning_forum.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostMapper postMapper;
    PostRepository postRepository;

    //Create post
    public PostResponse createPost(PostRequest request) {
        log.info("Create new post with userId: {}", request.getUserId());
        Post post = postMapper.toPost(request);
        if (request.getStatus() == null) {
            post.setStatus(STATUS.PENDING);
        }
        return postMapper.toPostResponse(postRepository.save(post));
    }

    //Approve post
    public PostResponse approvePost(ApprovePostRequest request) {
        log.info("Approve post with id: {}", request.getId());
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        post.setStatus(STATUS.APPROVED);
        post.setRejectReason(null);
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            post.setCategory(category);
        }
        return postMapper.toPostResponse(postRepository.save(post));
    }

    //Reject post
    public PostResponse rejectPost(RejectPostRequest request) {
        log.info("Reject post with id: {}", request.getId());
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        post.setStatus(STATUS.REJECTED);
        post.setRejectReason(request.getRejectReason());
        return postMapper.toPostResponse(postRepository.save(post));
    }

    // Get all posts
    public ListPostResponse getAllPosts(int page, int size, String sortBy, String order, String search) {
        log.info("getAllPosts with: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        int pageIndex = Math.max(page - 1, 0);

        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Post> spec = getPostSpecification(search);

        Page<Post> posts = postRepository.findAll(spec, pageable);
        List<PostResponse> post = posts.getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .toList();

        return new ListPostResponse(post, posts.getTotalElements(), posts.getTotalPages(), page, size);
    }

    // Specification tìm kiếm theo tiêu đề bài viết
    private static @NotNull Specification<Post> getPostSpecification(String search) {
        Specification<Post> spec = Specification.where(null); // không lọc mặc định gì cả

        if (search != null && !search.trim().isEmpty()) {
            Specification<Post> searchSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + search.toLowerCase() + "%"
                    );
            spec = spec.and(searchSpec);
        }

        return spec;
    }

}
