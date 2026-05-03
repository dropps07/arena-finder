package com.arenafinder.auth.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.arenafinder.auth.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Handles all JWT operations: generation and validation.
 *
 * WHAT IS A JWT?
 * ──────────────
 * A JWT (JSON Web Token) has 3 parts separated by dots:
 * header.payload.signature
 *
 * Header: algorithm used (HS256)
 * Payload: claims — userId, email, role, expiry (readable by anyone)
 * Signature: HMAC of header+payload using our secret key
 *
 * The signature is what makes it secure. Anyone can READ the payload
 * but cannot FORGE a new token without the secret key.
 * That's why the secret must never be committed to git.
 *
 * STATELESS AUTH:
 * The server never stores tokens. When a token arrives, we just verify
 * the signature. If valid, we trust the claims inside it.
 * This is what makes JWTs horizontally scalable — no shared session store.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * @Value injects values from application.properties / environment variables.
     *        "jwt.secret" maps to the JWT_SECRET env var we set in docker-compose.
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {

        // Keys.hmacShaKeyFor requires at least 32 bytes for HS256.
        // We enforce this in application.properties with a minimum length check.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT for a given user.
     * Called after successful login or registration.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId().toString()) // "sub" claim — who this token is for
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("name", user.getName())
                .issuedAt(now) // "iat" — when issued
                .expiration(expiry) // "exp" — when it expires
                .signWith(secretKey) // signs with HMAC-SHA256
                .compact(); // builds the final string
    }

    /**
     * Validates a token and returns all claims inside it.
     * Throws JwtException if the token is expired, tampered, or malformed.
     *
     * We return Claims (not boolean) so the caller gets user info
     * without needing to call another method.
     */
    public Claims validateAndExtract(String token) {
        // parseSignedClaims verifies the signature AND checks expiry.
        // If anything is wrong it throws — we catch in the controller.
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convenience method — extracts just the user ID from a token.
     * Used by other services when they receive a userId from the gateway.
     */
    public Long extractUserId(String token) {
        Claims claims = validateAndExtract(token);
        return Long.parseLong(claims.getSubject());
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
