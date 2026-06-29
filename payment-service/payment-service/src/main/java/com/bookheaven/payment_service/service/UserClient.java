package com.bookheaven.payment_service.service;

import com.bookheaven.common.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${api.gateway.url}")
    private String gatewayUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public UserResponse getUserById(UUID id) {
        String url = gatewayUrl + "/api/user/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Secret", serviceSecret);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to lookup user profile for ID: " + id, e);
        }
    }

    public UserResponse fallbackGetUserById(UUID id, Throwable t) {
        log.error("Circuit breaker tripped for getUserById", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is temporarily unavailable. Please try again later.", t);
    }
}
