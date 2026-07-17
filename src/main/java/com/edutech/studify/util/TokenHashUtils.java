package com.edutech.studify.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hashes opaque tokens before persistence - same "never store secrets in
 * plaintext" principle already applied to passwords via BCrypt. SHA-256
 * (not BCrypt) is used deliberately: a refresh token is already high-entropy
 * random data, not a human-chosen password, so we don't need BCrypt's slow,
 * salted hashing - just a fast, deterministic hash for equality lookup.
 */
@UtilityClass
public class TokenHashUtils {

    public String sha256(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available on this JVM", e);
        }
    }
}