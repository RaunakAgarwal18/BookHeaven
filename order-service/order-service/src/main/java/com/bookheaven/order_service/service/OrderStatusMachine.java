package com.bookheaven.order_service.service;

import com.bookheaven.order_service.entity.Order.OrderStatus;
import com.bookheaven.order_service.exception.InvalidOrderStatusException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED)
    );

    /**
     * Validates that the transition from currentStatus to targetStatus is allowed.
     * @throws InvalidOrderStatusException if the transition is not valid
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidOrderStatusException(
                    "Cannot transition from " + currentStatus + " to " + targetStatus
                            + ". Allowed transitions from " + currentStatus + ": "
                            + (allowed != null ? allowed : "none")
            );
        }
    }

    /**
     * Parses a status string into an OrderStatus enum.
     * @throws InvalidOrderStatusException if the string doesn't match any status
     */
    public OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid order status: " + status);
        }
    }
}
