package com.edutech.studify.service;

import com.edutech.studify.entity.User;

public interface RefreshTokenService {

    String createRefreshToken(User user, boolean terminateOtherSessions);

    User validateAndConsume(String rawToken);

    void revokeToken(String rawToken);

    void revokeAllUserTokens(User user);

    /**
     * Hard-deletes all revoked/expired tokens. Called by the scheduled
     * cleanup job - not meant to be invoked from request-handling code.
     */
    void purgeStaleTokens();
}