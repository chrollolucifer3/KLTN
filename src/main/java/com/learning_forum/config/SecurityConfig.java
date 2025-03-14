package com.learning_forum.config;

import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    CustomJwtDecoder customJwtDecoder;
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    String[] PUBLIC_URLS = {
            "/users",
            "/auth/login",
            "/auth/logout",
            "/auth/refresh"
    };

    /**
     * Cấu hình Spring Security cho ứng dụng:
     * - Cho phép tất cả mọi người truy cập `/users` và `/auth/login`.
     * - Các API còn lại yêu cầu xác thực (authenticated).
     * - Sử dụng OAuth2 Resource Server để xác thực JWT.
     * - Vô hiệu hóa CSRF (Cross-Site Request Forgery) do dùng JWT.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        JwtConfigurer -> JwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Không thêm "ROLE_"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role"); // Lấy "roles" từ JWT

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return authenticationConverter;
    }

    /**
     * Bean dùng để giải mã và xác thực JWT.
     * - Sử dụng NimbusJwtDecoder để kiểm tra tính hợp lệ của JWT.
     * - Dùng thuật toán HMAC SHA-256 để xác thực chữ ký của JWT.
     */

    //Lấy tài khoản đang đăng nhập
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return authentication.getName(); // Lấy username trực tiếp
    }

//    public String getCurrentUserRole() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//
//        return authentication.getAuthorities().stream()
//                .findFirst() // Lấy role đầu tiên (vì mỗi user chỉ có 1 role)
//                .map(GrantedAuthority::getAuthority) // Lấy tên quyền (VD: "ROLE_ADMIN")
//                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
//    }
}
