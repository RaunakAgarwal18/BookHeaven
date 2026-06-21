package com.example.demo.config;

import com.example.demo.component.SlidingWindowRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    // ==================== CONFIG PROPERTIES ====================

    @Value("${rate-limiter.login.capacity}")
    private int loginCapacity;
    @Value("${rate-limiter.login.refill-tokens}")
    private int loginRefillTokens;
    @Value("${rate-limiter.login.refill-duration-seconds}")
    private long loginRefillDurationSeconds;

    @Value("${rate-limiter.auth.capacity}")
    private int authCapacity;
    @Value("${rate-limiter.auth.refill-tokens}")
    private int authRefillTokens;
    @Value("${rate-limiter.auth.refill-duration-seconds}")
    private long authRefillDurationSeconds;

    @Value("${rate-limiter.checkout.capacity}")
    private int checkoutCapacity;
    @Value("${rate-limiter.checkout.refill-tokens}")
    private int checkoutRefillTokens;
    @Value("${rate-limiter.checkout.refill-duration-seconds}")
    private long checkoutRefillDurationSeconds;

    @Value("${rate-limiter.standard.capacity}")
    private int standardCapacity;
    @Value("${rate-limiter.standard.refill-tokens}")
    private int standardRefillTokens;
    @Value("${rate-limiter.standard.refill-duration-seconds}")
    private long standardRefillDurationSeconds;

    // ==================== KEY RESOLVERS ====================

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("Authorization")
        ).defaultIfEmpty(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-API-Key")
        ).defaultIfEmpty("anonymous");
    }

    // ==================== RATE LIMITERS ====================

    @Bean("loginRateLimiter")
    public SlidingWindowRateLimiter loginRateLimiter() {
        // Max 5 attempts per minute per IP to protect against credential stuffing
        return new SlidingWindowRateLimiter(loginCapacity, loginRefillTokens, Duration.ofSeconds(loginRefillDurationSeconds));
    }

    @Bean("authRateLimiter")
    public SlidingWindowRateLimiter authRateLimiter() {
        // Max 10 attempts per minute per IP for signups, OTP verification, forgot password
        return new SlidingWindowRateLimiter(authCapacity, authRefillTokens, Duration.ofSeconds(authRefillDurationSeconds));
    }

    @Bean("checkoutRateLimiter")
    public SlidingWindowRateLimiter checkoutRateLimiter() {
        // Max 5 checkouts per minute to prevent checkout spam/carding attacks
        return new SlidingWindowRateLimiter(checkoutCapacity, checkoutRefillTokens, Duration.ofSeconds(checkoutRefillDurationSeconds));
    }

    @Bean("standardRateLimiter")
    @Primary
    public SlidingWindowRateLimiter standardRateLimiter() {
        // General catalog browsing: Max 100 requests per minute
        return new SlidingWindowRateLimiter(standardCapacity, standardRefillTokens, Duration.ofSeconds(standardRefillDurationSeconds));
    }
}
