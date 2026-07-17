package com.edutech.studify.repository;

import com.edutech.studify.entity.RefreshToken;
import com.edutech.studify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);

    /**
     * All of a user's currently usable sessions - not revoked, not expired.
     * Ordered oldest-first so the caller can evict from the front when
     * enforcing the max-concurrent-sessions limit.
     */
    @Query("SELECT rt FROM RefreshToken rt " +
            "WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now " +
            "ORDER BY rt.createdAt ASC")
    List<RefreshToken> findActiveSessionsByUser(@Param("user") User user, @Param("now") Instant now);
}