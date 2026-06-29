package com.bookheaven.order_service.exception;

import com.bookheaven.common.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            OrderNotFoundException.class,
            AddressNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 🔴 403 - Forbidden
    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 🔴 409 - Conflict
    @ExceptionHandler({
            InsufficientStockException.class,
            EmptyCartException.class,
            OrderCancellationException.class,
            InvalidCouponException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 🔴 400 - Bad Request (cancellation window expired)
    @ExceptionHandler(OrderCancellationWindowException.class)
    public ResponseEntity<ErrorResponse> handleCancellationWindow(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 🔴 502 - External service failure
    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorResponse> handlePayment(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    // 🔴 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(ex.getMessage() != null ? ex.getMessage() : "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}


