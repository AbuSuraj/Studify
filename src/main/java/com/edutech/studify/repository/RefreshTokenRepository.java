package com.edutech.studify.repository;

import com.edutech.studify.entity.RefreshToken;
import com.edutech.studify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(@Param("user") User user);

    @Query("SELECT rt FROM RefreshToken rt " +
            "WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now " +
            "ORDER BY rt.createdAt ASC")
    List<RefreshToken> findActiveSessionsByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Hard-deletes any token that's no longer usable - either revoked
     * (rotated, logged out, or evicted) or naturally expired. Run
     * periodically by RefreshTokenCleanupJob to stop the table growing
     * unbounded.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiryDate < :now")
    int deleteStaleTokens(@Param("now") Instant now);
}