package com.learning_forum.service;

import com.learning_forum.dto.request.AuthenticationRequest;
import com.learning_forum.dto.request.LogoutRequest;
import com.learning_forum.dto.request.RefreshRequest;
import com.learning_forum.dto.respone.AuthenticationResponse;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.entity.InvalidatedToken;
import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.UserMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    // Login
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        UserResponse userResponse = userMapper.toUserResponseForUser(user);
        var token = generateToken(user);
        return new AuthenticationResponse(token, userResponse);
    }

    // Tạo token từ username của user (1 ngày hết hạn)
    private String generateToken(User user) {
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

    public SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
//        System.out.println("Received token: " + token);

        JWSVerifier verifier = new MACVerifier(secret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified = signedJWT.verify(verifier);

        // Nếu không phải refresh token thì kiểm tra hạn sử dụng
        if (!verified || (!isRefresh && signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date()))) {
            System.out.println("Token không hợp lệ hoặc đã hết hạn!");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra xem token có bị revoke không
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if (jwtId != null && invalidatedTokenRepository.existsById(jwtId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }


    //Logout
    public void logout(LogoutRequest request) throws ParseException, JOSEException {

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

    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
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
//        System.out.println("User: " + user);
        UserResponse userResponse = userMapper.toUserResponseForUser(user);
        var token = generateToken(user);
//        System.out.println(token);
        return new AuthenticationResponse(token, userResponse);
    }
}
