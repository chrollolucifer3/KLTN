package com.learning_forum.config;

import com.learning_forum.entity.User;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;


@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    @Lazy
    CustomJwtDecoder customJwtDecoder;
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    String[] PUBLIC_URLS = {
            "/users",
            "/auth/login",
            "/auth/logout",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/refresh",
            "auth/reset-password",
            "/admin/login",
            "/admin/logout",
            "/admin/refresh",
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
                .cors(Customizer.withDefaults()) // Sử dụng cấu hình CORS mặc định
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_URLS).permitAll() // Cho phép POST tới các URL public
                        .requestMatchers(HttpMethod.GET, "/post/**").permitAll() // Cho phép GET tới
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/category/all").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated() // Các request còn lại cần xác thực
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        JwtConfigurer -> JwtConfigurer // Cấu hình xác thực JWT
                        .decoder(customJwtDecoder) // Sử dụng custom JWT decoder
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Sử dụng custom JWT authentication converter
                ))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)) // Xử lý exception khi xác thực JWT thất bại

                .csrf(AbstractHttpConfigurer::disable); // Vô hiệu hóa CSRF

        return httpSecurity.build();
    }

    /**
     * Cấu hình JwtAuthenticationConverter để chuyển đổi JWT thành Authentication.
     * - Sử dụng JwtGrantedAuthoritiesConverter để lấy quyền từ JWT.
     * - Không thêm tiền tố "ROLE_" vào các quyền lấy từ JWT.
     * - Lấy quyền từ claim "role" trong JWT.
     */
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

//    //Lấy tài khoản đang đăng nhập
//    public String getCurrentUsername() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//
//        return authentication.getName(); // Lấy username trực tiếp
//    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    "", // Không cần mật khẩu
                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
        };
    }
}
