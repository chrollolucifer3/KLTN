package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.STATUS;
import com.learning_forum.domain.USER_ROLE;

import com.learning_forum.dto.projection.UserFollowerProjection;
import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;

import com.learning_forum.dto.respone.UserListResponse;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.dto.respone.UserResponseForAdmin;
import com.learning_forum.entity.Follow;
import com.learning_forum.entity.Post;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.UserMapper;
import com.learning_forum.repository.FollowRepository;
import com.learning_forum.repository.PostRepository;
import com.learning_forum.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityConfig securityConfig;
    PostRepository postRepository;
    FollowRepository followRepository;

    // Create user
    public UserResponse createUser(UserCreationRequest request) {
        log.info("Create User with request: {}", request);
        Map<String, String> errorMap = new HashMap<>();

        if (userRepository.existsUserByUsername(request.getUsername())) {
            errorMap.put("username", "Tài khoản đã tồn tại");
        }

        if (userRepository.existsUserByPhone(request.getPhone())) {
            errorMap.put("phone", "Số điện thoại đã tồn tại");
        }

        if (userRepository.existsUserByEmail(request.getEmail())) {
            errorMap.put("email", "Email đã tồn tại");
        }

        // Nếu có bất kỳ lỗi nào, ném ngoại lệ với thông tin lỗi dạng key-value
        if (!errorMap.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, errorMap);
        }

        User user = userMapper.toUser(request); // Chuyển UserCreationRequest -> User
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRole() == null) {
            user.setRole(USER_ROLE.USER);
        }

        if ((request.getIsActive() == null)) {
            user.setActive(true);
        }

        return userMapper.toUserResponseForUser(userRepository.save(user));
    }

    // Get all users
    public UserListResponse getAllUsers(int page, int size, String sortBy, String order, String search, Boolean isActive) {
        log.info("Getting users: page={}, size={}, sortBy={}, order={}, search={}, isActive={}",
                page, size, sortBy, order, search, isActive);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order.toUpperCase());

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<User> spec = getUserSpecification(search, isActive); // Gọi hàm mới có isActive

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponseForAdmin> users = userPage.getContent().stream()
                .map(userMapper::toUserResponseForAdmin)
                .toList();

        return new UserListResponse(users, userPage.getTotalElements(), userPage.getTotalPages(), page, size);
    }

    // Tạo Specification để lọc bỏ SUPER_ADMIN và tìm kiếm theo username, email, phone, fullName
    private static @NotNull Specification<User> getUserSpecification(String search, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc bỏ SUPER_ADMIN
            predicates.add(cb.notEqual(root.get("role"), "SUPER_ADMIN"));

            // 2. Tìm kiếm theo username, email, phone, fullName (không phân biệt hoa thường)
            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword),
                        cb.like(cb.lower(root.get("fullName")), keyword)
                );
                predicates.add(searchPredicate);
            }

            // 3. Lọc theo trạng thái isActive
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // Gộp tất cả điều kiện với AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    // Get user by id
    public UserResponseForAdmin getMyInfo() {
        log.info("UserService: Getting my info");
        String currentUsername = securityConfig.getCurrentUsername();
        return userMapper.toUserResponseForAdmin(userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

//    public void deleteUserById(String id) {
//        userRepository.deleteById(id);
//    }

    // Update user
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        log.info("UserService: Updating user {}", id);
        Map<String, String> errorMap = new HashMap<>();

        if (userRepository.existsUserByPhone(request.getPhone())) {
            errorMap.put("phone", "Số điện thoại đã tồn tại");
        }
        if (userRepository.existsUserByEmail(request.getEmail())) {
            errorMap.put("email", "Email đã tồn tại");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        request.setDob(Optional.ofNullable(request.getDob()).orElse(user.getDob()));
        request.setEmail(Optional.ofNullable(request.getEmail()).orElse(user.getEmail()));
        request.setPhone(Optional.ofNullable(request.getPhone()).orElse(user.getPhone()));
        request.setFullName(Optional.ofNullable(request.getFullName()).orElse(user.getFullName()));

        userMapper.updateUser(user, request);
        return userMapper.toUserResponseForUser(userRepository.save(user));
    }

    // Block user
    public void blockUser(String id) {
        log.info("UserService: Blocking user {}", id);

        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (currentUsername.equals(user.getUsername())) {
            log.info("User {} cannot block themselves", user.getUsername());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!user.isActive()) {
            log.info("User {} is already blocked", user.getUsername());
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (user.getRole() == USER_ROLE.SUPER_ADMIN) {
            log.info("User {} is super admin, cannot be blocked", user.getUsername());
            throw new AppException(ErrorCode.SUPER_ADMIN_BLOCKED);
        }

        if (user.getRole() == USER_ROLE.ADMIN && currentUser.getRole() != USER_ROLE.SUPER_ADMIN) {
            log.info("User {} does not have permission to block", currentUser);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        user.setActive(false);
        userRepository.save(user);

        List<Post> posts = postRepository.findAllByUserId(user.getId());
        for (Post post : posts) {
            if (post.getStatus() == STATUS.APPROVED) {
                post.setStatus(STATUS.BLOCKED);
            }
        }
        postRepository.saveAll(posts);
        log.info("User {} is blocked", user.getUsername());
    }

    // Unblock user
    public void unblockUser(String id) {
        log.info("UserService: Unblocking user {}", id);
        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.isActive()) {
            log.info("User {} is already unblocked", user.getUsername());
            throw new AppException(ErrorCode.USER_UNBLOCKED);
        }

        if (currentUser.getRole() != USER_ROLE.SUPER_ADMIN && user.getRole() == USER_ROLE.ADMIN) {
            log.info("User {} does not have permission to unblock", currentUser);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        user.setActive(true);
        userRepository.save(user);

        List<Post> posts = postRepository.findAllByUserId(user.getId());
        for (Post post : posts) {
            if (post.getStatus() == STATUS.BLOCKED) {
                post.setStatus(STATUS.APPROVED); // Hoặc trạng thái khác phù hợp
            }
        }
        postRepository.saveAll(posts);
        log.info("User {} is unblocked", user.getUsername());
    }

    // upload avatar
    public void uploadAvatar(String id, MultipartFile file) {
        log.info("UserService: Uploading avatar for user {}", id);

        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            log.info("User {} is blocked", user.getUsername());
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!currentUsername.equals(user.getUsername())) {
            log.info("User {} does not have permission to upload avatar for user {}", currentUser, user);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String UPLOAD_DIR = "uploads/avatars/";
        File uploadFile = new File(UPLOAD_DIR);

        if (!uploadFile.exists()) {
            uploadFile.mkdirs();
        }

        // Kiểm tra nếu file rỗng
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        // Kiểm tra định dạng file (chỉ chấp nhận JPG, PNG, JPEG, WEBP)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))
                && !contentType.equals("image/jpg") && !contentType.equals("image/webp")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Kiểm tra dung lượng file (giới hạn 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        try {
            // Xóa avatar cũ nếu tồn tại
            if (user.getAvatarUrl() != null) {
                Path oldAvatarPath = Paths.get(user.getAvatarUrl().substring(1)); // Bỏ dấu '/' đầu tiên
                File oldAvatarFile = oldAvatarPath.toFile();
                if (oldAvatarFile.exists()) {
                    oldAvatarFile.delete();  // Xóa file cũ
                    log.info("Deleted old avatar: {}", oldAvatarPath);
                }
            }

            // Lưu avatar mới
            String filename = id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());

            // Cập nhật avatarUrl
            user.setAvatarUrl("/" + UPLOAD_DIR + filename); // Thêm "/" để giữ đúng đường dẫn
            userRepository.save(user);
            log.info("Uploaded new avatar: {}", filePath);

        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    //Lấy số lượng người dùng mới trong tháng hiện tại
    public int getNewUsersCountThisMonth() {
        log.info("UserService: Getting new users count for this month");

        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);

        return userRepository.countNewUser(startOfMonth, endOfMonth);
    }

    public void followUser(String userId) {
        log.info("UserService: Following user {}", userId);

        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User userToFollow = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!currentUser.isActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }
        if (currentUser.getRole() == USER_ROLE.SUPER_ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (currentUser.getId().equals(userToFollow.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Kiểm tra đã follow chưa
        boolean isAlreadyFollowing = followRepository.existsByFollowerAndFollowing(currentUser, userToFollow);
        if (isAlreadyFollowing) {
            throw new AppException(ErrorCode.ALREADY_FOLLOWING);
        }

        // Tạo follow mới
        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(userToFollow)
                .build();

        followRepository.save(follow);

        log.info("User {} followed {}", currentUser.getUsername(), userToFollow.getUsername());
    }

    // Unfollow user
    public void unfollowUser(String userId) {
        log.info("UserService: Unfollowing user {}", userId);

        String currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User userToUnfollow = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!currentUser.isActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }
        if (currentUser.getRole() == USER_ROLE.SUPER_ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (currentUser.getId().equals(userToUnfollow.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        // Kiểm tra xem đã follow chưa
        Follow follow = followRepository.findByFollowerAndFollowing(currentUser, userToUnfollow)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOLLOWING));
        // Xóa follow
        followRepository.delete(follow);
    }
    // Get following users
    public UserListResponse getFollowingUsers(int page, int size, String sortBy, String order) {
        log.info("UserService: Getting following users with page: {}, size: {}, sortBy: {}, order: {}", page, size, sortBy, order);

        String currentUsername = securityConfig.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!currentUser.isActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        int pageIndex = Math.max(page - 1, 0);
        int offset = pageIndex * size;

        //  Chuẩn hóa sortBy
        String orderColumn = switch (sortBy) {
            case "username" -> "u.username";
            case "createdAt" -> "u.created_at";
            default -> "u.created_at"; // mặc định
        };

        //  Chuẩn hóa thứ tự sắp xếp
        String orderDirection = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";

        // Gọi repository để lấy danh sách users kèm số lượng followers/post
        List<UserFollowerProjection> projectionList = followRepository.findFollowingUsersWithStatsDynamicOrder(
                currentUser.getId(), orderColumn, orderDirection, size, offset
        );

        // Map projection → response
        List<UserResponseForAdmin> userResponses = projectionList.stream()
                .map(p -> UserResponseForAdmin.builder()
                        .id(p.getId())
                        .username(p.getUsername())
                        .email(p.getEmail())
                        .phone(p.getPhone())
                        .fullName(p.getFullName())
                        .dob(p.getDob())
                        .role(USER_ROLE.valueOf(p.getRole()))
                        .avatarUrl(p.getAvatarUrl())
                        .isActive(p.getIsActive())
                        .createdAt(p.getCreatedAt())
                        .postCount(p.getPostCount())
                        .followerCount(p.getFollowersCount())
                        .build()
                ).toList();

        Long total = followRepository.countFollowingByUserId(currentUser.getId());

        return UserListResponse.builder()
                .users(userResponses)
                .total(total)
                .totalPages((int) Math.ceil((double) total / size))
                .page(page)
                .size(size)
                .build();
    }

    // get user by id
    public UserResponseForAdmin getUserById(String userId) {
        log.info("UserService: Getting user by id {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        int postCount = postRepository.countApprovedPostsByUserId(userId).intValue();
        int followerCount = followRepository.countFollowersByUserId(userId).intValue();

        return UserResponseForAdmin.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .dob(user.getDob())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .createdAt(LocalDate.from(user.getCreatedAt()))
                .postCount(postCount)
                .followerCount(followerCount)
                .build();
    }

}
