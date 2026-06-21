package com.bookheaven.cart_service.client;

import com.bookheaven.cart_service.dto.cartResponseDto.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookClientImpl implements BookClient {

    private final RestTemplate restTemplate;

    @Value("${book.service.bulk.url}")
    private String bookServiceUrl;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @Override
    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackGetBooksByIds")
    public List<BookDto> getBooksByIds(List<Long> bookIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Service-Secret", serviceSecret);
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
            if (credentials != null) {
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + credentials.toString());
            }
        }
        HttpEntity<List<Long>> request = new HttpEntity<>(bookIds, headers);
        ResponseEntity<BookDto[]> response = restTemplate.exchange(
                bookServiceUrl,
                HttpMethod.POST,
                request,
                BookDto[].class
        );
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public List<BookDto> fallbackGetBooksByIds(List<Long> bookIds, Throwable t) {
        log.error("Circuit breaker tripped for getBooksByIds", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Book service is temporarily unavailable. Please try again later.", t);
    }
}
