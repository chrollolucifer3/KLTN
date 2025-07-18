package com.learning_forum.service;

import com.learning_forum.config.SecurityConfig;
import com.learning_forum.config.SecurityUtil;
import com.learning_forum.domain.USER_ROLE;
import com.learning_forum.dto.request.*;
import com.learning_forum.dto.respone.AuthAdminResponse;
import com.learning_forum.dto.respone.AuthenticationResponse;
import com.learning_forum.entity.InvalidatedToken;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.repository.InvalidatedTokenRepository;
import com.learning_forum.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    @NonFinal
    @Value("${jwt.secret}")
    protected String secret;

    @NonFinal
    @Value("${jwt.expiration}")
    protected Long EXPIRATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${jwt.EXPIRATION_MINUTES}")
    protected Long EXPIRATION_MINUTES;

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;
    EmailService emailService;
    @Lazy
    SecurityUtil securityConfig;

    // Login for user
    public AuthenticationResponse login(AuthenticationRequest request) {
        log.info("Login with request: {}", request);
        User user = userRepository.findByUsernameAndRole(request.getUsername(), USER_ROLE.USER)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        var token = generateToken(user);
        return new AuthenticationResponse(token);
    }

    // Login for admin
    public AuthAdminResponse loginAdmin(AuthenticationRequest request) {
        log.info("Login with request: {}", request);
        Optional<User> userOpt = userRepository.findByUsernameAndRole(request.getUsername(), USER_ROLE.ADMIN)
                .or(() -> userRepository.findByUsernameAndRole(request.getUsername(), USER_ROLE.SUPER_ADMIN));

        User user = userOpt.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        var token = generateToken(user);
        return new AuthAdminResponse(token);
    }

    // Tạo token từ username của user (1 ngày hết hạn)
    private String generateToken(User user) {
        log.info("Generating token for user: {}", user);
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("learning-forum")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(EXPIRATION, ChronoUnit.DAYS).toEpochMilli()
                ))
                .claim("role", user.getRole())
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secret.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    // Verify token
    public SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        log.info("Verifying token");
        JWSVerifier verifier = new MACVerifier(secret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                    .plus(REFRESHABLE_DURATION, ChronoUnit.DAYS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verify = signedJWT.verify(verifier);
        if(!verify && expiryTime.after(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){ // Kiểm tra token đã logout chưa
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    //Logout
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        log.info("Logout request: {}", request);
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .token(request.getToken())
                    .build();
            invalidatedTokenRepository.save(invalidatedToken); // Lưu token khi logout vào database
        } catch (AppException e) {
            log.info("Token is invalid");
        }
    }

    // Refresh token for user
    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        log.info("Refreshing token: {}", request);
        System.out.println(request.getToken());
        var signedJWT = verifyToken(request.getToken() , true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken); // Lưu token cũ vào database

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var token = generateToken(user);
        return new AuthenticationResponse(token);
    }

    // Refresh token for admin
    public AuthAdminResponse refreshTokenAdmin(RefreshRequest request)
            throws ParseException, JOSEException {
        log.info("Refreshing token for Admin: {}", request);
        var signedJWT = verifyToken(request.getToken() , true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken); // Lưu token cũ vào database

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var token = generateToken(user);
        return new AuthAdminResponse(token);
    }

    // Update password
    public void updatePassword( UserUpdatePasswordRequest request) {
        String currentUsername = securityConfig.getCurrentUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Register new user
    public void register(UserCreationRequest request) {
        log.info("Register new user with request: {}", request);
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
        // Mã hóa mật khẩu trước khi lưu
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() == null) {
            request.setRole(USER_ROLE.USER); // Mặc định là USER nếu không có role
        }
        if (request.getIsActive() == null) {
            request.setIsActive(true); // Mặc định là active nếu không có trạng thái
        }
        // Lưu người dùng mới vào cơ sở dữ liệu
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(request.getIsActive())
                .build();
        userRepository.save(user);
    }

    // quên mật khẩu
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password for user with email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        String token = generateTokenForgotPassword(user.getEmail());

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
    }

    private String generateTokenForgotPassword(String email) {
        try {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issuer("learning-forum")
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("type", "forgot_password")
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            Payload payload = new Payload(jwtClaimsSet.toJSONObject());
            JWSObject jwsObject = new JWSObject(header, payload);
            jwsObject.sign(new MACSigner(secret.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }

    // Reset password
    public void resetPassword(ResetPasswordRequest request) throws ParseException, JOSEException {
        log.info("Reset password for user with token: {}", request.getToken());

        // Kiểm tra token đã bị vô hiệu hóa chưa
        if (invalidatedTokenRepository.existsByToken(request.getToken())) {
            throw new AppException(ErrorCode.TOKEN_INVALIDATED);
        }
        // Xác thực token
        SignedJWT signedJWT = verifyToken(request.getToken(), false);
        String email = signedJWT.getJWTClaimsSet().getSubject();
        // Lấy người dùng từ email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!request.getPasswordNew().equals(request.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getPasswordNew()));
        userRepository.save(user);

        // Vô hiệu hóa token đã sử dụng, lưu trữ vào InvalidatedToken
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(signedJWT.getJWTClaimsSet().getJWTID())
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
    }
}
