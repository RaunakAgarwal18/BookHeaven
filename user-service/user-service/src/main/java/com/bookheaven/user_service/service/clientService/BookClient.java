package com.bookheaven.user_service.service.clientService;

import com.bookheaven.common.dto.response.BookPublicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookClient {

    private final RestTemplate restTemplate;

    @Value("${internal.service.secret}")
    private String serviceSecret;

    @Value("${book.service.bulk-books-url}")
    private String bulkBooksUrl;

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackGetBulkBooks")
    public List<BookPublicResponse> getBulkBooks(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String url = bulkBooksUrl;
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Service-Secret", serviceSecret);
            
            HttpEntity<List<Long>> request = new HttpEntity<>(bookIds, headers);
            
            ResponseEntity<List<BookPublicResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<BookPublicResponse>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch bulk books from book-service for wishlist", e);
            throw e;
        }
    }

    public List<BookPublicResponse> fallbackGetBulkBooks(List<Long> bookIds, Throwable t) {
        log.error("Circuit breaker tripped for getBulkBooks", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Book service is temporarily unavailable. Please try again later.", t);
    }
}
