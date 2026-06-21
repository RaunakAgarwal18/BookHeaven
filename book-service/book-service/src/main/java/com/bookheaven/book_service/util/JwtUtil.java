package com.bookheaven.book_service.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public UUID extractId(String token){
        return UUID.fromString(getClaims(token).getSubject());
    }
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getKey())
                .build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
}
