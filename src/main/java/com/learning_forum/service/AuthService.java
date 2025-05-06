package com.learning_forum.service;

import com.learning_forum.domain.USER_ROLE;
import com.learning_forum.dto.request.AuthenticationRequest;
import com.learning_forum.dto.request.LogoutRequest;
import com.learning_forum.dto.request.RefreshRequest;
import com.learning_forum.dto.request.UserUpdatePasswordRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


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

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

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
        log.info("Verifying token: {}", token);
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
    public void updatePassword(String id, UserUpdatePasswordRequest request) {
        log.info("Update password for user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
