package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.domain.USER_ROLE;

import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;

import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.dto.respone.UserResponseForAdmin;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.UserMapper;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityConfig securityConfig;

    // Create user
    public UserResponse createUser(UserCreationRequest request) {

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
    public List<UserResponseForAdmin> getAllUsers() {
        return userRepository.findAll()
                .stream() // Chuyển List<User> -> Stream<User>
                .filter(user -> user.getRole() != USER_ROLE.SUPER_ADMIN)
                .map(userMapper::toUserResponseForAdmin)  // Chuyển User -> UserResponse
                .collect(Collectors.toList()); // Chuyển Stream<UserResponse> -> List<UserResponse>
    }

    // Get user by id
    public UserResponse getMyInfo() {
        String currentUsername = securityConfig.getCurrentUsername();
        return userMapper.toUserResponseForUser(userRepository.findByUsername(currentUsername)
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
        log.info("User {} is blocked", user.getUsername());
    }

    // Unblock user
    public void unblockUser(String id) {
        log.info("UserService: Unblocking user {}", id);
        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
        log.info("User {} is unblocked", user.getUsername());
    }

    // upload avatar
    public void uploadAvatar(String id, MultipartFile file) {
        log.info("UserService: Uploading avatar for user {}", id);

        String currentUsername = securityConfig.getCurrentUsername();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
}
