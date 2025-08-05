package com.example.demo.security.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long jwtExpirationInMs;
    private final long refreshTokenExpirationInMs;
    
    public JwtTokenProvider(
            @Value("${jwt.secret:mySecretKey}") String jwtSecret,
            @Value("${jwt.expiration:86400000}") long jwtExpirationInMs,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpirationInMs) {
        
        // Ensure the secret key is at least 256 bits (32 bytes) for HS512
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) { // HS512 requires at least 512 bits (64 bytes)
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.refreshTokenExpirationInMs = refreshTokenExpirationInMs;
        
        log.info("JWT Token Provider initialized with expiration: {} ms", jwtExpirationInMs);
    }
    
    /**
     * Generate JWT token from authentication
     */
    public String generateToken(Authentication authentication) {
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("firstName", userPrincipal.getFirstName());
        claims.put("lastName", userPrincipal.getLastName());
        claims.put("authorities", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Authentication authentication) {
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpirationInMs);
        
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("tokenType", "refresh")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
    
    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Get user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("userId", Long.class);
    }
    
    /**
     * Get expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration();
    }
    
    /**
     * Get expiration time in seconds from JWT token
     */
    public Long getExpirationTimeFromToken(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.getTime() / 1000;
    }
    
    /**
     * Get token expiration time in seconds
     */
    public Long getExpirationTime() {
        return jwtExpirationInMs / 1000;
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String tokenType = claims.get("tokenType", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception ex) {
            log.error("Invalid refresh token: {}", ex.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }
    
    /**
     * Get all claims from token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}