package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.ApprovePostRequest;
import com.learning_forum.dto.request.PostRequest;
import com.learning_forum.dto.request.RejectPostRequest;
import com.learning_forum.dto.respone.ListPostResponse;
import com.learning_forum.dto.respone.ListPostResponseForAdmin;
import com.learning_forum.dto.respone.PostFromCategoryResponse;
import com.learning_forum.dto.respone.PostResponse;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.PostMapper;
import com.learning_forum.repository.CategoryRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostMapper postMapper;
    PostRepository postRepository;
    SecurityConfig securityConfig;
    UserRepository userRepository;
    CategoryRepository categoryRepository;

    //Create post
    public PostResponse createPost(PostRequest request) {
        log.info("Create new post with userId: {}", request.getUserId());
        Post post = postMapper.toPost(request);

        if (request.getStatus() == null) {
            post.setStatus(STATUS.PENDING);
        }

        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            post.setCategory(category);
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
    public ListPostResponseForAdmin getAllPosts(int page, int size, String sortBy, String order, String search) {
        log.info("getAllPosts with: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        int pageIndex = Math.max(page - 1, 0);

        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Post> spec = getPostSpecification(search);

        Page<Post> posts = postRepository.findAll(spec, pageable);

        List<PostResponse> postResponses = posts.getContent()
                .stream()
                .map(postMapper::toPostResponseForAdmin)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(postResponses, posts.getTotalElements(), posts.getTotalPages(), page, size);
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

    // Get all posts by category
    public ListPostResponse getAllPostsByCategory(String id, int page, int size, String sortBy, String order) {
        log.info("getAllPostsByCategory with: {}, size: {}, sortBy: {}, order: {}", id, size, sortBy, order);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<Post> posts = postRepository.findAllByCategoryIdAndStatus(id, STATUS.APPROVED, pageable);

        List<PostFromCategoryResponse> postResponseList = posts.getContent()
                .stream()
                .map(postMapper::toPostFromCategoryResponse)
                .toList();

        // Lấy tên danh mục
        var categoryName = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
                .getName();

        return new ListPostResponse(
                categoryName,
                postResponseList,
                posts.getTotalElements(),
                posts.getTotalPages(),
                page,
                size
        );
    }

    // Get post by id
    public PostResponse getPostById(String id) {
        log.info("Get post by id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        boolean isLiked = false;
        String currentUsername = securityConfig.getCurrentUsername();

        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            isLiked = post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        }

        PostResponse response = postMapper.toPostResponse(post);
        response.setLiked(isLiked);
        response.setLikeCount(post.getLikesCount());

        return response;
    }

    // Lấy 5 bài viết mới nhất
    public List<PostResponse> getPost() {
        log.info("Get latest posts");
        List<Post> posts = postRepository.findTop5ByStatusOrderByCreatedAtDesc(STATUS.APPROVED);
        return posts.stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
    }
}
