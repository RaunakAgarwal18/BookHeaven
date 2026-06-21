package com.example.demo.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JWTTokenValidatorFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    public JWTTokenValidatorFilter() {
        System.out.println("FILTER CREATED");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        String serviceSecretHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Service-Secret");

        if (serviceSecretHeader != null && serviceSecretHeader.equals(internalServiceSecret)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "SYSTEM",
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_SYSTEM")
            );
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        String jwt = null;
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            HttpCookie cookie = exchange.getRequest()
                    .getCookies()
                    .getFirst("accessToken");
            if (cookie != null) {
                jwt = cookie.getValue();
            }
        }

        if (jwt == null || jwt.trim().isEmpty() || jwt.equals("null") || jwt.equals("undefined")) {
            return chain.filter(exchange);
        }

        try {

            SecretKey secretKey = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String email = String.valueOf(claims.get("email"));

            var auth = new UsernamePasswordAuthenticationToken(
                    email,
                    "Bearer " + jwt,
                    AuthorityUtils.NO_AUTHORITIES
            );

            // Mutate request to inject Authorization header for downstream microservices
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange)
                    .contextWrite(
                            ReactiveSecurityContextHolder.withAuthentication(auth)
                    );
        } catch (Exception ex) {
            // Invalid or expired token — do NOT short-circuit with an error.
            // Pass through without setting authentication:
            log.warn("Invalid or expired JWT, continuing without authentication: {}", ex.getMessage());
            return chain.filter(exchange);
        }
    }
}