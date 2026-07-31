package com.tenpearls.contactmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final javax.crypto.SecretKey key;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationInMs) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT signing secret key cannot be null or empty. Please configure app.jwt.secret.");
        }
        try {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (io.jsonwebtoken.security.WeakKeyException ex) {
            throw new IllegalStateException("app.jwt.secret must be at least 256 bits (32 bytes) for HS256", ex);
        }
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    /**
     * Generates a token for a user session.
     *
     * @param authentication the authentication object
     * @return the generated JWT token, or null if principal is incompatible
     */
    public String generateToken(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.error("Authentication principal is not an instance of UserDetails");
            throw new IllegalArgumentException("Invalid authentication principal");
        }
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Retrieves username/identifier from the token.
     *
     * @param token the JWT token
     * @return the subject username, or null if token is invalid
     */
    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse JWT token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Validate the token and catch all JJWT parsing/validation exceptions.
     *
     * @param authToken the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }
}
