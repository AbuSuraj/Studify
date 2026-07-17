package com.edutech.studify.service;

import com.edutech.studify.entity.User;

public interface RefreshTokenService {

    /**
     * Creates and persists a new refresh token for the given user.
     *
     * @param terminateOtherSessions if true, every other existing session for
     *                               this user is revoked first (single-session
     *                               login). If false, other sessions are left
     *                               alone unless the max-concurrent-session
     *                               limit is reached, in which case the oldest
     *                               session is evicted to make room.
     * @return the raw (unhashed) token to hand back to the client
     */
    String createRefreshToken(User user, boolean terminateOtherSessions);

    User validateAndConsume(String rawToken);

    void revokeToken(String rawToken);

    void revokeAllUserTokens(User user);
}