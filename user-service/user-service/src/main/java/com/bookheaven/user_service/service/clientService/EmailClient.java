package com.bookheaven.user_service.service.clientService;

import com.bookheaven.user_service.dto.requestDto.PasswordResetRequest;
import com.bookheaven.user_service.dto.requestDto.SendOtpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailClient {
    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Service-Secret", serviceSecret);
        return headers;
    }

    @Async
    @CircuitBreaker(name = "emailService", fallbackMethod = "fallbackSendOtp")
    public void sendOtp(SendOtpRequest request) {
        HttpEntity<SendOtpRequest> entity = new HttpEntity<>(request, createHeaders());
        restTemplate.exchange(
                emailServiceUrl + "/send-otp",
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    public void fallbackSendOtp(SendOtpRequest request, Throwable t) {
        log.error("Circuit breaker tripped for sendOtp. OTP email was not sent.", t);
        // We log and return silently because this is an @Async method returning void
    }

    @Async
    @CircuitBreaker(name = "emailService", fallbackMethod = "fallbackSendPasswordResetMail")
    public void sendPasswordResetMail(PasswordResetRequest request) {
        HttpEntity<PasswordResetRequest> entity = new HttpEntity<>(request, createHeaders());
        restTemplate.exchange(
                emailServiceUrl + "/send-password-reset",
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    public void fallbackSendPasswordResetMail(PasswordResetRequest request, Throwable t) {
        log.error("Circuit breaker tripped for sendPasswordResetMail. Reset email was not sent.", t);
        // We log and return silently because this is an @Async method returning void
    }
}
