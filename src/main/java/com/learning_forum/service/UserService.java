package com.learning_forum.service;

import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.UserMapper;
import com.learning_forum.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    public UserResponse createUser(UserCreationRequest request) {

        Map<String, String> errorMap = new HashMap<>();

        if (userRepository.existsUserByUsername(request.getUsername())) {
            errorMap.put("username", "Username already existed");
        }
        if (userRepository.existsUserByPhone(request.getPhone())) {
            errorMap.put("phone", "Phone already existed");
        }
        if(userRepository.existsUserByEmail(request.getEmail())) {
            errorMap.put("email", "Email already existed");
        }

        // Nếu có bất kỳ lỗi nào, ném ngoại lệ với thông tin lỗi dạng key-value
        if (!errorMap.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, errorMap);
        }

        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    };

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (userRepository.existsUserByPhone(request.getPhone())) {
            errorMap.put("phone", "Số điện thoại đã tồn tại");
        }
        if(userRepository.existsUserByEmail(request.getEmail())) {
            errorMap.put("email", "Email đã tồn tại");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Tài Khoản không tồn tại"));
        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
