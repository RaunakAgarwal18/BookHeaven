package com.bookheaven.order_service.service.clientService;

import com.bookheaven.common.dto.request.InitiatePaymentRequest;
import com.bookheaven.order_service.dto.paymentRequestDto.RefundPaymentRequest;
import com.bookheaven.common.dto.response.InitiatePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackInitiatePayment")
    public InitiatePaymentResponse initiatePayment(UUID orderId, UUID userId, Double amount, String currency, String paymentMethod, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.add("X-Service-Secret", serviceSecret);
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setOrderId(orderId);
        request.setUserId(userId);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setPaymentMethod(paymentMethod);
        HttpEntity<InitiatePaymentRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<InitiatePaymentResponse> response = restTemplate.exchange(
                paymentServiceUrl + "/api/payment/initiate",
                HttpMethod.POST,
                entity,
                InitiatePaymentResponse.class
        );
        return response.getBody();
    }

    public InitiatePaymentResponse fallbackInitiatePayment(UUID orderId, UUID userId, Double amount, String currency, String paymentMethod, String token, Throwable t) {
        log.error("Circuit breaker tripped for initiatePayment", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackRefund")
    public void refund(UUID orderId, Double amount, String reason) {
        RefundPaymentRequest request =  RefundPaymentRequest.builder()
                                            .orderId(orderId)
                                            .amount(amount)
                                            .reason(reason)
                                            .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Service-Secret", serviceSecret);
        HttpEntity<RefundPaymentRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.exchange(
                paymentServiceUrl + "/api/payment/refund",
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    public void fallbackRefund(UUID orderId, Double amount, String reason, Throwable t) {
        log.error("Circuit breaker tripped for refund", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment service is temporarily unavailable. Please try again later.", t);
    }
}
