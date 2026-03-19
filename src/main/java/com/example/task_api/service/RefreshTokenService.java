package com.example.task_api.service;

import com.example.task_api.exception.BadRequestException;
import com.example.task_api.model.RefreshToken;
import com.example.task_api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    // 7 days expiry
    private static final long REFRESH_EXPIRATION =
            7 * 24 * 60 * 60;

    // =====================================================
    // CREATE TOKEN
    // =====================================================

    public RefreshToken createRefreshToken(String userEmail) {

        RefreshToken token = RefreshToken.builder()
                .userEmail(userEmail)
                .token(UUID.randomUUID().toString())
                .expiryDate(
                        Instant.now().plusSeconds(REFRESH_EXPIRATION)
                )
                .build();

        return repo.save(token);
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public RefreshToken verifyToken(String token) {

        RefreshToken refreshToken =
                repo.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"));

        if (refreshToken.getExpiryDate()
                .isBefore(Instant.now())) {

            repo.delete(refreshToken);

            throw new BadRequestException(
                    "Refresh token expired");
        }

        return refreshToken;
    }

    // =====================================================
    // DELETE USER TOKENS (LOGOUT SUPPORT)
    // =====================================================

    public void deleteByUser(String userEmail) {
        repo.deleteByUserEmail(userEmail);
    }
}