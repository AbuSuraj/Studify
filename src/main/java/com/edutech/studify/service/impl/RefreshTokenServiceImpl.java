package com.edutech.studify.service.impl;

import com.edutech.studify.entity.RefreshToken;
import com.edutech.studify.entity.User;
import com.edutech.studify.exception.TokenRefreshException;
import com.edutech.studify.repository.RefreshTokenRepository;
import com.edutech.studify.service.RefreshTokenService;
import com.edutech.studify.util.TokenHashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.refresh-token.max-active-sessions:3}")
    private int maxActiveSessions;

    @Override
    @Transactional
    public String createRefreshToken(User user, boolean terminateOtherSessions) {
        if (terminateOtherSessions) {
            revokeAllUserTokens(user);
        } else {
            enforceSessionLimit(user);
        }

        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtils.sha256(rawToken))
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
     * Keeps a user at or under maxActiveSessions by revoking their oldest
     * active session(s) if the new login would push them over the limit.
     */
    private void enforceSessionLimit(User user) {
        List<RefreshToken> activeSessions = refreshTokenRepository.findActiveSessionsByUser(user, Instant.now());

        int sessionsToEvict = activeSessions.size() - maxActiveSessions + 1; // +1 makes room for the incoming login
        if (sessionsToEvict <= 0) {
            return;
        }

        for (int i = 0; i < sessionsToEvict; i++) {
            RefreshToken oldest = activeSessions.get(i);
            oldest.setRevoked(true);
            refreshTokenRepository.save(oldest);
        }

        log.info("User {} hit the {}-session limit - evicted {} oldest session(s).",
                user.getEmail(), maxActiveSessions, sessionsToEvict);
    }

    @Override
    @Transactional
    public User validateAndConsume(String rawToken) {
        String tokenHash = TokenHashUtils.sha256(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not recognized. Please login again."));

        if (storedToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token has been revoked. Please login again.");
        }

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new TokenRefreshException("Refresh token has expired. Please login again.");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return storedToken.getUser();
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = TokenHashUtils.sha256(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }
}