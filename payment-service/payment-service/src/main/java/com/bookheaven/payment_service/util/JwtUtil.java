package com.bookheaven.payment_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.public.key}")
    private String publicKeyPem;

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

    public UUID extractId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

        public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getPublicKey())
                .build().parseSignedClaims(token).getPayload();
    }
}
