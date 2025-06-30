package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.*;
import com.learning_forum.entity.Category;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.PostLike;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.PostMapper;
import com.learning_forum.repository.CategoryRepository;
import com.learning_forum.repository.FollowRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
import jakarta.persistence.criteria.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
    FollowRepository followRepository;
    NotificationService notificationService;

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

        post = postRepository.save(post);

        return postMapper.toPostResponse(post);
    }

    //Approve post
    public PostResponse approvePost(ApprovePostRequest request) {
        log.info("Approve post with id: {}", request.getId());
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        post.setStatus(STATUS.APPROVED);
        post.setRejectReason(null);

        post = postRepository.save(post);

        // 1. Chuyển đổi Post sang PostResponse
        PostResponse postResponse = postMapper.toPostResponse(post);
        // 2. Gửi thông báo realtime đến followers
        notificationService.notifyFollowersNewPost(post.getUser().getId(), postResponse);
        return postResponse;
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
    public ListPostResponseForAdmin getAllPosts(int page, int size, String sortBy, String order, String search, String status) {
        log.info("getAllPosts with: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);
        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Post> spec = getPostSpecification(search, status);

        Page<Post> posts = postRepository.findAll(spec, pageable);

        List<PostResponse> postResponses = posts.getContent()
                .stream()
                .map(postMapper::toPostResponseForAdmin)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(postResponses, posts.getTotalElements(), posts.getTotalPages(), page, size);
    }

    // Specification tìm kiếm theo tiêu đề bài viết
    private static @NotNull Specification<Post> getPostSpecification(String search, String status) {
        Specification<Post> spec = Specification.where(null);

        // Tìm kiếm theo tiêu đề
        if (search != null && !search.trim().isEmpty()) {
            Specification<Post> searchSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
            spec = spec.and(searchSpec);
        }

        // Lọc theo status
        if (status != null && !status.trim().isEmpty()) {
            Specification<Post> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        return spec;
    }

    public ListPostResponseForAdmin getAllPostsForClient(int page, int size, String sortBy, String order, String search) {
        log.info("getAllPosts with: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<Post> posts;

        if (search != null && !search.trim().isEmpty()) {
            posts = postRepository.findByStatusAndTitleContainingIgnoreCase(STATUS.APPROVED, search, pageable);
        } else {
            posts = postRepository.findAllByStatus(STATUS.APPROVED, pageable);
        }

        List<PostResponse> postResponses = posts.getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(
                postResponses,
                posts.getTotalElements(),
                posts.getTotalPages(),
                page,
                size
        );
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

    public PostResponse getPostById(String id) {
        log.info("Get post by id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        boolean isLiked = false;
        boolean isFollowingAuthor = false;

        String currentUsername = securityConfig.getCurrentUsername();

        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            isLiked = post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));

            //  Check follow bằng FollowRepository
            isFollowingAuthor = followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), post.getUser().getId());
        }

        post.setViewsCount(post.getViewsCount() + 1);
        postRepository.save(post);

        PostResponse response = postMapper.toPostResponse(post);
        response.setLiked(isLiked);
        response.setLikesCount(post.getLikesCount());
        response.setFollowingAuthor(isFollowingAuthor);
        response.setAuthorFollowersCount(followRepository.countByFollowingId(post.getUser().getId()));

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

    public ListFivePostResponse getFivePostMostLiked() {
        log.info("Get top 5 most liked posts overall");
        Pageable topFive = PageRequest.of(0, 5);

        List<Post> posts = postRepository.findTop5PostByLikesCount(STATUS.APPROVED.name());

        List<PostResponse> postResponses = posts.stream()
                .map(postMapper::toPostResponse)
                .toList();

        return new ListFivePostResponse(postResponses);
    }

    // Lấy số lượng bài viết mới trong tháng hiện tại
    public int getCountPostsThisMonth() {
        log.info("Get new posts count in current month");
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);
        return postRepository.countByCreatedAtAfterAndStatus(startOfMonth, endOfMonth, STATUS.APPROVED);
    }

    public List<CountPostsByTimeResponse> getCountPostsByTime(TimeRangeRequest request) {
        log.info("PostService.GetCountPostsByTime with request: {}", request);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Khớp với FE gửi lên

        // Parse startDate, endDate từ String → LocalDate → LocalDateTime
        LocalDate startDate = LocalDate.parse(request.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), formatter);

        // Đổi sang LocalDateTime để truy vấn chính xác theo ngày giờ
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        List<Object[]> results = postRepository.countPostsByTimeRange(startDateTime, endDateTime);

        return results.stream()
                .map(result -> new CountPostsByTimeResponse(
                        String.valueOf(result[0]),  // Chuyển result[0] sang String
                        ((Number) result[1]).intValue()
                ))
                .collect(Collectors.toList());
    }

    public ListPostResponseForAdmin getAllPostsByUser(int page, int size, String sortBy, String order, String status) {
        log.info("Get all posts by user with: page: {}, size: {}, sortBy: {}, order: {}, status: {}", page, size, sortBy, order, status);

        String currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<Post> posts;

        // Nếu có status -> lọc theo status, ngược lại lấy tất cả
        if (status != null && !status.isEmpty()) {
            posts = postRepository.findAllByUserIdAndStatus(currentUser.getId(), STATUS.valueOf(status), pageable);
        } else {
            posts = postRepository.findAllByUserId(currentUser.getId(), pageable);
        }

        List<PostResponse> postResponses = posts.getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(postResponses, posts.getTotalElements(), posts.getTotalPages(), page, size);
    }

    //delete post
    public void deletePost(String id) {
        log.info("Delete post with id: {}", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        postRepository.delete(post);
    }

    // get posts by userId
    public ListPostResponseForAdmin getPostsByUserId(String userId, int page, int size, String sortBy, String order, STATUS status) {
        log.info("Get posts by userId: {}, page: {}, size: {}, sortBy: {}, order: {}, status: {}", userId, page, size, sortBy, order, status);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Page<Post> posts;

        if (status != null) {
            posts = postRepository.findAllByUserIdAndStatus(userId, STATUS.APPROVED, pageable);
        } else {
            posts = postRepository.findAllByUserId(userId, pageable);
        }

        List<PostResponse> postResponses = posts.getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(postResponses, posts.getTotalElements(), posts.getTotalPages(), page, size);
    }

    // Update post
    public void updatePost(UpdatePostRequest request) {
        log.info("Update post with id: {}", request.getId());
        Post post = postRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        var currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra quyền cập nhật bài viết
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Cập nhật các trường cần thiết
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        }

        postRepository.save(post);
    }

    // Lấy bài viết đã thích của người dùng
    public ListPostResponseForAdmin getLikedPosts(int page, int size, String sortBy, String order, String search) {
        log.info("Get liked posts - page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        String currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        log.info("Current User ID = {}", currentUser.getId());

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.fromString(order), sortBy));

        Specification<Post> spec = (root, query, cb) -> {
            Subquery<String> subquery = query.subquery(String.class);
            Root<PostLike> likeRoot = subquery.from(PostLike.class);

            subquery.select(likeRoot.get("post").get("id"))
                    .where(cb.equal(likeRoot.get("user").get("id"), currentUser.getId()));

            Predicate inLikedPosts = root.get("id").in(subquery);

            // 👉 Không phải do bản thân viết
            Predicate notSelfAuthor = cb.notEqual(root.get("user").get("id"), currentUser.getId());

            Predicate titlePredicate = null;
            if (search != null && !search.trim().isEmpty()) {
                titlePredicate = cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase().trim() + "%");
            }

            Predicate finalPredicate = cb.and(inLikedPosts, notSelfAuthor);
            if (titlePredicate != null) {
                finalPredicate = cb.and(finalPredicate, titlePredicate);
            }

            return finalPredicate;
        };

        Page<Post> likedPosts = postRepository.findAll(spec, pageable);
        log.info("Found {} liked posts (excluding own)", likedPosts.getTotalElements());

        List<PostResponse> postResponses = likedPosts.getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        return new ListPostResponseForAdmin(
                postResponses,
                likedPosts.getTotalElements(),
                likedPosts.getTotalPages(),
                page,
                size
        );
    }

}
