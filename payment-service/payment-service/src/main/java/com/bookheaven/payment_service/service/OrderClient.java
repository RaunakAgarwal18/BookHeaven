package com.bookheaven.payment_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
public class OrderClient {

    private final RestTemplate restTemplate;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackConfirmOrder")
    public void confirmOrder(UUID orderId, String gatewayPaymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Secret", serviceSecret);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                orderServiceUrl + orderId + "/confirm?paymentId=" + gatewayPaymentId,
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void fallbackConfirmOrder(UUID orderId, String gatewayPaymentId, Throwable t) {
        log.error("Circuit breaker tripped for confirmOrder", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackFailOrder")
    public void failOrder(UUID orderId, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Secret", serviceSecret);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                orderServiceUrl + orderId + "/fail?reason=" + reason,
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void fallbackFailOrder(UUID orderId, String reason, Throwable t) {
        log.error("Circuit breaker tripped for failOrder", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackCompleteRefund")
    public void completeRefund(UUID orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Secret", serviceSecret);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                orderServiceUrl + orderId + "/refund-complete",
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void fallbackCompleteRefund(UUID orderId, Throwable t) {
        log.error("Circuit breaker tripped for completeRefund", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order service is temporarily unavailable. Please try again later.", t);
    }
}