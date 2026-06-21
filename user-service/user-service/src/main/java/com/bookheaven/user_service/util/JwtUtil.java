package com.bookheaven.user_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    public String generateAccessToken(UUID id, String email, String userName, String role){
        return Jwts.builder()
                .subject(id.toString())
                .claim("email", email)
                .claim("username", userName)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration)) //1 day
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(UUID id, String email, String userName, String role){
        return Jwts.builder()
                .subject(id.toString())
                .claim("email", email)
                .claim("username", userName)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration*7)) // 7 days
                .signWith(getKey())
                .compact();
    }
    public UUID extractId(String token){
        return UUID.fromString(getClaims(token).getSubject());
    }
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
    public String extractUsername(String token){
        return getClaims(token).get("username", String.class);
    }
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getKey())
                .build().parseSignedClaims(token).getPayload();
    }

}
