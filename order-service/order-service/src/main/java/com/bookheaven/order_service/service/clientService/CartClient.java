package com.bookheaven.order_service.service.clientService;

import com.bookheaven.order_service.dto.cartResponseDto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartClient {

    private final RestTemplate restTemplate;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Secret", serviceSecret);
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
            if (credentials != null) {
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + credentials.toString());
            }
        }
        return headers;
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "fallbackGetCart")
    public CartResponse getCart() {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<CartResponse> response = restTemplate.exchange(
                cartServiceUrl,
                HttpMethod.GET,
                request,
                CartResponse.class
        );
        return response.getBody();
    }

    public CartResponse fallbackGetCart(Throwable t) {
        log.error("Circuit breaker tripped for getCart", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cart service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "fallbackClearCart")
    public void clearCart(UUID userId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                cartServiceUrl + "/internal/" + userId,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

    public void fallbackClearCart(UUID userId, Throwable t) {
        log.error("Circuit breaker tripped for clearCart", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cart service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "fallbackIncrementCouponUsage")
    public void incrementCouponUsage(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) return;
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);
        String couponUrl = "http://CART-SERVICE/api/coupons/" + couponCode + "/increment-usage";
        try {
            restTemplate.exchange(
                    couponUrl,
                    HttpMethod.POST,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            // Log but do not fail order confirmation
            log.error("Failed to increment coupon usage: " + e.getMessage());
        }
    }

    public void fallbackIncrementCouponUsage(String couponCode, Throwable t) {
        log.error("Circuit breaker tripped for incrementCouponUsage", t);
        // Do not fail the order just because coupon usage increment failed
    }
}