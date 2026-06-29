package com.bookheaven.user_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.private.key}")
    private String privateKeyPem;

    @Value("${jwt.public.key}")
    private String publicKeyPem;

    @Value("${jwt.expiration}")
    private long expiration;

    // Helper to parse the Base64 string into a Java PrivateKey object
    private PrivateKey getPrivateKey() {
        try {
            byte[] encoded = Base64.getDecoder().decode(privateKeyPem);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse private key", e);
        }
    }

    // Helper to parse the Base64 string into a Java PublicKey object
    private PublicKey getPublicKey() {
        try {
            byte[] encoded = Base64.getDecoder().decode(publicKeyPem);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse public key", e);
        }
    }
    public String generateAccessToken(UUID id, String email, String userName, String role){
        return Jwts.builder()
                .subject(id.toString())
                .claim("email", email)
                .claim("username", userName)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration)) //1 day
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
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
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
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
        return Jwts.parser().verifyWith(getPublicKey())
                .build().parseSignedClaims(token).getPayload();
    }

}
