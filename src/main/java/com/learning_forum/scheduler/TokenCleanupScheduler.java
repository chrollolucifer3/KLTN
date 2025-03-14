package com.learning_forum.scheduler;

import com.learning_forum.repository.InvalidatedTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class TokenCleanupScheduler {

    InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(fixedRate = 36000000) // Chạy mỗi 10 tiếng
    @Transactional
    public void cleanExpiredTokens() {
        int deletedCount = invalidatedTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
        System.out.println("Deleted " + deletedCount + " expired tokens.");
    }
}
