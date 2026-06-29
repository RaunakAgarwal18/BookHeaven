package com.bookheaven.order_service.service.clientService;

import com.bookheaven.common.dto.response.UserResponse;
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
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    private String getBaseUrl() {
        // userServiceUrl is configured as http://USER-SERVICE/api/user/me
        // We strip /me to get the base endpoint http://USER-SERVICE/api/user
        return userServiceUrl.replace("/me", "");
    }

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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    public UserResponse getUser() {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                getBaseUrl() + "/me",
                HttpMethod.GET,
                request,
                UserResponse.class
        );
        return response.getBody();
    }

    public UserResponse fallbackGetUser(Throwable t) {
        log.error("Circuit breaker tripped for getUser", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    public UserResponse getUserById(UUID id) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                getBaseUrl() + "/" + id,
                HttpMethod.GET,
                request,
                UserResponse.class
        );
        return response.getBody();
    }

    public UserResponse fallbackGetUserById(UUID id, Throwable t) {
        log.error("Circuit breaker tripped for getUserById", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "User service is temporarily unavailable. Please try again later.", t);
    }
}
