package com.edutech.studify.scheduler;

import com.edutech.studify.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${app.jwt.refresh-token.cleanup-cron:0 0 3 * * *}")
    public void run() {
        log.info("Running scheduled refresh token cleanup...");
        refreshTokenService.purgeStaleTokens();
    }
}