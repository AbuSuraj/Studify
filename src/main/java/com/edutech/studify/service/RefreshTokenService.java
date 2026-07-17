package com.edutech.studify.service;

import com.edutech.studify.entity.User;

public interface RefreshTokenService {

    /**
     * Creates and persists a new refresh token for the given user.
     * @return the raw (unhashed) token to hand back to the client
     */
    String createRefreshToken(User user);

    /**
     * Validates a raw refresh token (must exist, not be revoked, not be
     * expired) and atomically revokes it - each refresh token is single-use.
     * @return the user the token belonged to
     */
    User validateAndConsume(String rawToken);

    /** Revokes a single refresh token (used for logout). */
    void revokeToken(String rawToken);

    /** Revokes every refresh token belonging to a user. */
    void revokeAllUserTokens(User user);
}