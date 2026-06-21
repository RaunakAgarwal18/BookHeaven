package com.example.demo.component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("slidingWindowRateLimiter")
public class SlidingWindowRateLimiter extends AbstractRateLimiter<SlidingWindowRateLimiter.Config> {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    private final int capacity;
    private final int refillTokens;
    private final Duration refillDuration;

    public SlidingWindowRateLimiter() {
        super(Config.class, "sliding-window-rate-limiter", null);
        this.capacity = 100;
        this.refillTokens = 100;
        this.refillDuration = Duration.ofMinutes(1);
    }

    public SlidingWindowRateLimiter(int capacity, int refillTokens, Duration refillDuration) {
        super(Config.class, "sliding-window-rate-limiter", null);
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        // Cache key should be unique per route and client key to prevent cross-contamination
        String cacheKey = routeId + ":" + id;
        Bucket bucket = buckets.computeIfAbsent(cacheKey, key -> createSlidingWindowBucket());

        boolean allowed = bucket.tryConsume(1);
        long remainingTokens = bucket.getAvailableTokens();

        // Build response headers
        Map<String, String> headers = Map.of(
                "X-RateLimit-Remaining", String.valueOf(remainingTokens),
                "X-RateLimit-Allowed",   String.valueOf(allowed)
        );

        return Mono.just(new Response(allowed, headers));
    }

    private Bucket createSlidingWindowBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(capacity)
                                .refillGreedy(refillTokens, refillDuration)
                                .build()
                )
                .build();
    }

    @Getter
    @Setter
    public static class Config {
        private int capacity = 100;
        private int refillTokens = 100;
        private Duration refillDuration = Duration.ofMinutes(1);
    }
}