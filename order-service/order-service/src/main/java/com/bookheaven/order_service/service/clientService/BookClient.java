package com.bookheaven.order_service.service.clientService;

import com.bookheaven.common.dto.response.BookDto;
import com.bookheaven.common.dto.request.StockUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookClient {

    private final RestTemplate restTemplate;

    @Value("${book.service.url}")
    private String bookServiceUrl;

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

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackGetBulkBooks")
    public List<BookDto> getBulkBooks(List<Long> bookIds) {
        HttpHeaders headers = createHeaders();
        HttpEntity<List<Long>> request = new HttpEntity<>(bookIds, headers);
        BookDto[] response = restTemplate.postForObject(
                bookServiceUrl + "/bulk",
                request,
                BookDto[].class
            );
        return response != null ? List.of(response) : List.of();
    }

    public List<BookDto> fallbackGetBulkBooks(List<Long> bookIds, Throwable t) {
        log.error("Circuit breaker tripped for getBulkBooks", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Book service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackReduceStock")
    public void reduceStock(List<StockUpdateRequest> items) {
        HttpHeaders headers = createHeaders();
        HttpEntity<List<StockUpdateRequest>> request = new HttpEntity<>(items, headers);
        restTemplate.exchange(
                bookServiceUrl + "/reduce-stock",
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void fallbackReduceStock(List<StockUpdateRequest> items, Throwable t) {
        log.error("Circuit breaker tripped for reduceStock", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Book service is temporarily unavailable. Please try again later.", t);
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackRestoreStock")
    public void restoreStock(List<StockUpdateRequest> items) {
        HttpHeaders headers = createHeaders();
        HttpEntity<List<StockUpdateRequest>> request = new HttpEntity<>(items, headers);
        restTemplate.exchange(
                bookServiceUrl + "/restore-stock",
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    public void fallbackRestoreStock(List<StockUpdateRequest> items, Throwable t) {
        log.error("Circuit breaker tripped for restoreStock", t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Book service is temporarily unavailable. Please try again later.", t);
    }
}

