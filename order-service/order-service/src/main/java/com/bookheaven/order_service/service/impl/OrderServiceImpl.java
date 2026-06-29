package com.bookheaven.order_service.service.impl;

import com.bookheaven.common.dto.event.*;
import com.bookheaven.order_service.dto.OrderResponseDto.OrderResponse;
import com.bookheaven.common.dto.response.BookDto;
import com.bookheaven.common.dto.request.StockUpdateRequest;
import com.bookheaven.common.dto.response.CartItemResponse;
import com.bookheaven.common.dto.response.CartResponse;
import com.bookheaven.order_service.dto.checkoutRequestDto.CheckoutRequest;
import com.bookheaven.order_service.dto.checkoutResponseDto.CheckoutResponse;
import com.bookheaven.common.dto.response.InitiatePaymentResponse;
import com.bookheaven.common.dto.response.AddressResponse;
import com.bookheaven.common.dto.response.UserResponse;
import com.bookheaven.order_service.entity.Order;
import com.bookheaven.order_service.entity.OrderItem;
import com.bookheaven.order_service.entity.ShippingAddress;
import com.bookheaven.order_service.exception.*;
import com.bookheaven.order_service.repository.OrderRepository;
import com.bookheaven.order_service.service.OrderEventMapper;
import com.bookheaven.order_service.service.OrderService;
import com.bookheaven.order_service.service.OrderStatusMachine;
import com.bookheaven.order_service.service.clientService.BookClient;
import com.bookheaven.order_service.service.clientService.CartClient;
import com.bookheaven.order_service.service.clientService.PaymentClient;
import com.bookheaven.order_service.service.clientService.UserClient;
import com.bookheaven.order_service.util.AppUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final BookClient bookClient;
    private final OrderEventProducer orderEventProducer;
    private final OrderStatusMachine orderStatusMachine;
    private final OrderEventMapper orderEventMapper;

    @Value("${pricing.tax.rate:0.18}")
    private double taxRate;

    @Value("${pricing.shipping.rate:0.06}")
    private double shippingRate;

    // ===================== CHECKOUT =====================

    @Transactional
    public CheckoutResponse checkout(Authentication authentication, CheckoutRequest checkoutRequest) {
        String token = (String) authentication.getCredentials();
        UUID userId = (UUID) authentication.getPrincipal();

        // 1. Fetch cart
        CartResponse cart = cartClient.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot place order with empty cart");
        }

        // 2. Validate stock
        cart.getItems().forEach(item -> {
            if (item.getQuantity() > item.getAvailable()) {
                throw new InsufficientStockException(
                        "Insufficient stock for book: " + item.getTitle() +
                                ". Requested: " + item.getQuantity() +
                                ", Available: " + item.getAvailable()
                );
            }
        });

        // 3. Fetch user for address
        UserResponse user = userClient.getUser();
        if (user.getAddresses() == null || user.getAddresses().isEmpty()) {
            throw new AddressNotFoundException("Please add a delivery address before placing an order");
        }
        
        AddressResponse selectedAddress = user.getAddresses().stream()
                .filter(a -> a.getId().equals(checkoutRequest.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException("Selected address not found"));

        // 4. Fetch bulk books for seller info
        List<Long> listingIds = cart.getItems().stream()
                .map(CartItemResponse::getListingId)
                .toList();
        List<BookDto> books = bookClient.getBulkBooks(listingIds);
        Map<Long, BookDto> bookMap = books.stream()
                .collect(Collectors.toMap(BookDto::getListingId, b -> b));

        // 4b. Build order items snapshot
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> {
                    BookDto bookDto = bookMap.get(item.getListingId());
                    String sellerEmail = null;
                    if (bookDto != null && bookDto.getSellerId() != null) {
                        try {
                            UserResponse sellerProfile = userClient.getUserById(bookDto.getSellerId());
                            if (sellerProfile != null) {
                                sellerEmail = sellerProfile.getEmail();
                            }
                        } catch (Exception e) {
                            // Don't crash checkout if seller profile lookup fails
                        }
                    }
                    return OrderItem.builder()
                            .listingId(item.getListingId())
                            .bookId(item.getBookId())
                            .title(item.getTitle())
                            .author(item.getAuthor())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .currency(item.getCurrency())
                            .sellerId(bookDto != null ? bookDto.getSellerId() : null)
                            .sellerUsername(bookDto != null ? bookDto.getSellerUsername() : null)
                            .sellerEmail(sellerEmail)
                            .build();
                })
                .toList();

        // 5. Build shipping address snapshot
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .street(selectedAddress.getStreet())
                .city(selectedAddress.getCity())
                .state(selectedAddress.getState())
                .zipCode(selectedAddress.getZipCode())
                .country(selectedAddress.getCountry())
                .phoneNumber(user.getPhoneNumber())
                .build();

        // 5b. Calculate totals using externalized rates
        double subtotal = cart.getSubtotal() != null ? cart.getSubtotal() : cart.getTotalAmount();
        double discountAmount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0.0;
        String couponCode = cart.getCouponCode();
        
        double taxableAmount = Math.max(0.0, subtotal - discountAmount);
        double taxAmount = taxableAmount * taxRate;
        double shippingAmount = taxableAmount * shippingRate;
        double finalTotal = Math.round((taxableAmount + taxAmount + shippingAmount) * 100.0) / 100.0;

        String orderReference = generateUniqueOrderReference();

        // 6. Create order with PENDING status
        Order order = Order.builder()
                .userId(userId)
                .username(user.getUsername())
                .orderReference(orderReference)
                .shippingAddress(shippingAddress)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .couponCode(couponCode)
                .discountSellerId(cart.getDiscountSellerId())
                .taxAmount(taxAmount)
                .shippingAmount(shippingAmount)
                .totalAmount(finalTotal)
                .currency(cart.getCurrency())
                .paymentMethod(checkoutRequest.getPaymentMethod())
                .status(Order.OrderStatus.PENDING)
                .email(user.getEmail())
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);
        Order savedOrder = orderRepository.saveAndFlush(order);
        orderEventProducer.publishOrderTimeoutEvent(new OrderTimeoutEvent(savedOrder.getId()));

        // 6.5 Reserve stock immediately before payment!
        List<StockUpdateRequest> stockUpdates = cart.getItems().stream()
                .map(item -> new StockUpdateRequest(item.getListingId(), item.getQuantity()))
                .toList();
        try {
            bookClient.reduceStock(stockUpdates);
        } catch (Exception e) {
            savedOrder.setStatus(Order.OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new InsufficientStockException("One or more books are out of stock. Please try again later.");
        }

        // Reserve Coupon Usage
        if (order.getCouponCode() != null) {
            try {
                cartClient.incrementCouponUsage(order.getCouponCode());
            } catch (Exception e) {
                // Rollback stock!
                try {
                    bookClient.restoreStock(stockUpdates);
                } catch(Exception ex) {}
                savedOrder.setStatus(Order.OrderStatus.FAILED);
                orderRepository.save(savedOrder);
                throw new InvalidCouponException("Failed to apply coupon. Usage limit reached.");
            }
        }

        // 7. Initiate payment
        InitiatePaymentResponse paymentResponse;
        try {
            paymentResponse = paymentClient.initiatePayment(
                    savedOrder.getId(),
                    userId,
                    savedOrder.getTotalAmount(),
                    savedOrder.getCurrency(),
                    checkoutRequest.getPaymentMethod(),
                    token
            );
        } catch (Exception e) {
            try {
                bookClient.restoreStock(stockUpdates);
            } catch (Exception ex) {
                log.error("Failed to restore stock after payment initiation failure for order: {}", savedOrder.getId());
            }
            if (savedOrder.getCouponCode() != null) {
                cartClient.decrementCouponUsage(savedOrder.getCouponCode());
            }
            savedOrder.setStatus(Order.OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new PaymentServiceException("Failed to initiate payment, please try again");
        }

        // 8. Return checkout response with payment details
        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .orderReference(savedOrder.getOrderReference())
                .gatewayOrderId(paymentResponse.getGatewayOrderId())
                .keyId(paymentResponse.getKeyId())
                .totalAmount(savedOrder.getTotalAmount())
                .currency(savedOrder.getCurrency())
                .status(savedOrder.getStatus())
                .build();
    }

    // ===================== CONFIRM ORDER =====================

    @Transactional
    public void confirmOrder(UUID orderId, String gatewayPaymentId) {
        log.info("Confirming order id: {}, payment id: {}", orderId, gatewayPaymentId);
        Order order = getOrderById(orderId);

        // Idempotency guard
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            log.warn("Order {} is already CONFIRMED, skipping duplicate confirmOrder call", orderId);
            return;
        }

        // State-Machine guard (The Late Payment Fix)
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            log.error("Late payment received for order {} which is in status {}. Auto-refunding.", orderId, order.getStatus());
            
            order.setStatus(Order.OrderStatus.REFUND_IN_PROGRESS);
            order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.REFUND_IN_PROGRESS));
            orderRepository.save(order);
            
            RefundEvent refundEvent = RefundEvent.builder()
                    .eventId(UUID.randomUUID())
                    .orderId(orderId)
                    .amount(order.getTotalAmount())
                    .reason("Late payment: Order expired.")
                    .build();
            orderEventProducer.publishRefundEvent(refundEvent);
            return;
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.CONFIRMED));
        order.setPaymentId(gatewayPaymentId);
        orderRepository.save(order);

        try {
            cartClient.clearCart(order.getUserId());
        } catch (Exception e) {
            // don't fail order if cart clear fails
        }

        // Publish buyer confirmation email
        try {
            orderEventProducer.publishOrderConfirmedEvent(orderEventMapper.buildConfirmedEvent(order));
            log.info("Published OrderConfirmedEvent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderConfirmedEvent for order: {}", order.getId(), e);
        }

        // Publish seller notification emails — one per seller
        publishSellerNotifications(order);

        // Publish ledger event
        try {
            log.info("Publishing OrderLedgerEvent for order: {}", order.getId());
            orderEventProducer.publishOrderLedgerEvent(orderEventMapper.buildLedgerEvent(order));
        } catch (Exception e) {
            log.error("Failed to publish OrderLedgerEvent for order: {}", order.getId(), e);
        }
    }

    @Transactional
    public void failOrder(UUID orderId, String reason) {
        Order order = getOrderById(orderId);
        
        // State-Machine guard (The Late Decline Fix)
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            log.warn("Cannot fail order {} because it is already in status {}", orderId, order.getStatus());
            return;
        }
        
        if (order.getStatus() == Order.OrderStatus.FAILED) {
            return;
        }
        
        order.setStatus(Order.OrderStatus.FAILED);
        order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.FAILED));
        orderRepository.save(order);
        
        List<StockUpdateRequest> stockUpdates = order.getItems().stream()
                .map(item -> new StockUpdateRequest(item.getListingId(), item.getQuantity()))
                .toList();
        try {
            bookClient.restoreStock(stockUpdates);
        } catch (Exception e) {
            log.error("Failed to restore stock for failed order: {}", orderId, e);
        }
        
        if (order.getCouponCode() != null) {
            cartClient.decrementCouponUsage(order.getCouponCode());
        }
    }

    @Override
    @Transactional
    public void completeRefund(UUID orderId) {
        log.info("Completing refund for order: {}", orderId);
        Order order = getOrderById(orderId);
        
        boolean hasItemsRefunding = false;
        
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == Order.OrderStatus.REFUND_IN_PROGRESS) {
                item.setStatus(Order.OrderStatus.REFUNDED);
                hasItemsRefunding = true;
            }
        }
        
        if (hasItemsRefunding) {
            boolean allRefunded = order.getItems().stream()
                    .allMatch(i -> i.getStatus() == Order.OrderStatus.REFUNDED || i.getStatus() == Order.OrderStatus.CANCELLED);
            if (allRefunded) {
                order.setStatus(Order.OrderStatus.REFUNDED);
            } else {
                order.setStatus(Order.OrderStatus.PARTIALLY_REFUNDED);
            }
            orderRepository.save(order);
            log.info("Order {} refund complete. New status: {}", orderId, order.getStatus());
        }
    }

    // ===================== GET ORDER =====================

    @Override
    public OrderResponse getOrder(Authentication authentication, UUID orderId) {
        UUID userId = (UUID) authentication.getPrincipal();
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You are not allowed to view this order");
        }
        return AppUtil.toOrderResponse(order);
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // ===================== GET MY ORDERS =====================

    @Override
    public Page<OrderResponse> getMyOrders(Authentication authentication, int pageNumber, int pageSize, String status) {
        UUID userId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        if (status != null && !status.isBlank()) {
            Order.OrderStatus orderStatus = orderStatusMachine.parseStatus(status);
            return orderRepository.findByStatusAndUserId(orderStatus, userId, pageable)
                    .map(AppUtil::toOrderResponse);
        }

        return orderRepository.findByUserId(userId, pageable)
                .map(AppUtil::toOrderResponse);
    }

    // ===================== CANCEL ORDER =====================

    private static final int CANCELLATION_WINDOW_DAYS = 7;

    /**
     * Enforces the 1-week cancellation window. Admins intentionally bypass this.
     */
    private void assertCancellationWindowOpen(Order order) {
        if (order.getCreatedAt().isBefore(LocalDateTime.now().minusDays(CANCELLATION_WINDOW_DAYS))) {
            throw new OrderCancellationWindowException(
                    "Cancellation window has closed. Orders can only be cancelled within "
                    + CANCELLATION_WINDOW_DAYS + " days of placement.");
        }
    }

    /**
     * Full-order cancellation — used by User cancelOrder() and Admin updateOrderStatus().
     * Admins bypass the cancellation window but go through all the same financial cleanup.
     */
    private OrderResponse processFullOrderCancellation(Order order, Order.OrderStatus newStatus) {
        Order.OrderStatus oldOrderStatus = order.getStatus();
        
        // Capture items that are still active (not already cancelled/refunded by a seller)
        List<OrderItem> activeItems = order.getItems().stream()
                .filter(item -> item.getStatus() != Order.OrderStatus.CANCELLED
                             && item.getStatus() != Order.OrderStatus.REFUNDED
                             && item.getStatus() != Order.OrderStatus.REFUND_IN_PROGRESS)
                .toList();

        // Capture stock updates before items are mutated in step 2
        List<StockUpdateRequest> stockUpdates = activeItems.stream()
                .filter(item -> item.getStatus() == Order.OrderStatus.PENDING 
                             || item.getStatus() == Order.OrderStatus.CONFIRMED)
                .map(item -> new StockUpdateRequest(item.getListingId(), item.getQuantity()))
                .toList();

        // 1. REFUND FIRST (Event dispatch)
        if (oldOrderStatus == Order.OrderStatus.CONFIRMED
                || oldOrderStatus == Order.OrderStatus.SHIPPED
                || oldOrderStatus == Order.OrderStatus.DELIVERED
                || oldOrderStatus == Order.OrderStatus.PARTIALLY_REFUND_IN_PROGRESS
                || oldOrderStatus == Order.OrderStatus.PARTIALLY_REFUNDED) {
            newStatus = Order.OrderStatus.REFUND_IN_PROGRESS;
            
            double itemSubtotal = activeItems.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            double proportion   = itemSubtotal / order.getSubtotal();
            
            double discount = 0.0;
            if (order.getCouponCode() != null && order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                if (order.getDiscountSellerId() != null) {
                    double sellerSubtotalForDiscountedItems = order.getItems().stream()
                            .filter(i -> i.getSellerId() != null && i.getSellerId().equals(order.getDiscountSellerId()))
                            .mapToDouble(i -> i.getPrice() * i.getQuantity())
                            .sum();
                            
                    double activeDiscountedItemsSubtotal = activeItems.stream()
                            .filter(i -> i.getSellerId() != null && i.getSellerId().equals(order.getDiscountSellerId()))
                            .mapToDouble(i -> i.getPrice() * i.getQuantity())
                            .sum();
                            
                    if (sellerSubtotalForDiscountedItems > 0) {
                        double discountedProportion = activeDiscountedItemsSubtotal / sellerSubtotalForDiscountedItems;
                        discount = order.getDiscountAmount() * discountedProportion;
                    }
                } else {
                    discount = order.getDiscountAmount() * proportion;
                }
            }
            
            double tax          = (order.getTaxAmount() != null ? order.getTaxAmount() : 0.0) * proportion;
            double shipping     = 0.0;
            if (oldOrderStatus != Order.OrderStatus.SHIPPED && oldOrderStatus != Order.OrderStatus.DELIVERED) {
                shipping = (order.getShippingAmount() != null ? order.getShippingAmount() : 0.0) * proportion;
            }
            double refundAmount = Math.round((itemSubtotal - discount + tax + shipping) * 100.0) / 100.0;
            
            RefundEvent refundEvent = RefundEvent.builder()
                    .eventId(UUID.randomUUID())
                    .orderId(order.getId())
                    .amount(refundAmount)
                    .reason("Order cancelled")
                    .build();
            orderEventProducer.publishRefundEvent(refundEvent);
        }

        // 2. UPDATE DATABASE
        order.setStatus(newStatus);
        Order.OrderStatus finalNewStatus = newStatus;
        order.getItems().forEach(item -> item.setStatus(finalNewStatus));
        orderRepository.save(order);

        // 3. RESTORE STOCK (only if the books never physically left the warehouse)
        if (!stockUpdates.isEmpty()) {
            try {
                bookClient.restoreStock(stockUpdates);
            } catch (Exception e) {
                log.error("CRITICAL: Stock restore failed for cancelled order {}", order.getId(), e);
            }
        }

        // 4. RESTORE COUPON (best effort)
        if (order.getCouponCode() != null) {
            cartClient.decrementCouponUsage(order.getCouponCode());
        }

        // 5. VOID LEDGER ENTRIES (safe — 7-day window < 14-day payout cycle guarantees entries are still PENDING)
        try {
            OrderLedgerEvent voidEvent = OrderLedgerEvent.builder()
                    .orderId(order.getId())
                    .reversal(true)
                    .build();
            orderEventProducer.publishOrderLedgerEvent(voidEvent);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish ledger void for order {}", order.getId(), e);
        }

        return AppUtil.toOrderResponse(order);
    }

    /**
     * Partial-order cancellation — used by Seller updateSellerOrderStatus().
     * Issues a proportionally apportioned partial refund and voids only those ledger items.
     */
    private Order.OrderStatus processPartialOrderCancellation(Order order, List<OrderItem> sellerItems, Order.OrderStatus requestedStatus) {
        Order.OrderStatus itemNewStatus = requestedStatus;
        
        // Capture stock updates before items are mutated
        List<StockUpdateRequest> stockUpdates = sellerItems.stream()
                .filter(item -> item.getStatus() == Order.OrderStatus.PENDING 
                             || item.getStatus() == Order.OrderStatus.CONFIRMED)
                .map(i -> new StockUpdateRequest(i.getListingId(), i.getQuantity()))
                .toList();

        // 1. PARTIAL REFUND (proportional to seller's share of the subtotal)
        if (order.getStatus() == Order.OrderStatus.CONFIRMED
                || order.getStatus() == Order.OrderStatus.SHIPPED
                || order.getStatus() == Order.OrderStatus.DELIVERED
                || order.getStatus() == Order.OrderStatus.PARTIALLY_REFUND_IN_PROGRESS
                || order.getStatus() == Order.OrderStatus.PARTIALLY_REFUNDED) {
            double itemSubtotal = sellerItems.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            double proportion   = itemSubtotal / order.getSubtotal();
            
            double discount = 0.0;
            if (order.getCouponCode() != null && order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                if (order.getDiscountSellerId() != null) {
                    double sellerSubtotalForDiscountedItems = order.getItems().stream()
                            .filter(i -> i.getSellerId() != null && i.getSellerId().equals(order.getDiscountSellerId()))
                            .mapToDouble(i -> i.getPrice() * i.getQuantity())
                            .sum();
                            
                    double activeDiscountedItemsSubtotal = sellerItems.stream()
                            .filter(i -> i.getSellerId() != null && i.getSellerId().equals(order.getDiscountSellerId()))
                            .mapToDouble(i -> i.getPrice() * i.getQuantity())
                            .sum();
                            
                    if (sellerSubtotalForDiscountedItems > 0) {
                        double discountedProportion = activeDiscountedItemsSubtotal / sellerSubtotalForDiscountedItems;
                        discount = order.getDiscountAmount() * discountedProportion;
                    }
                } else {
                    discount = order.getDiscountAmount() * proportion;
                }
            }
            
            double tax          = (order.getTaxAmount() != null ? order.getTaxAmount() : 0.0) * proportion;
            double shipping     = 0.0;
            if (order.getStatus() != Order.OrderStatus.SHIPPED && order.getStatus() != Order.OrderStatus.DELIVERED) {
                shipping = (order.getShippingAmount() != null ? order.getShippingAmount() : 0.0) * proportion;
            }
            double refundAmount = Math.round((itemSubtotal - discount + tax + shipping) * 100.0) / 100.0;

            itemNewStatus = Order.OrderStatus.REFUND_IN_PROGRESS;
            sellerItems.forEach(item -> item.setStatus(Order.OrderStatus.REFUND_IN_PROGRESS));
            
            boolean allItemsRefundInProgress = order.getItems().stream()
                    .allMatch(item -> item.getStatus() == Order.OrderStatus.REFUND_IN_PROGRESS);
                    
            if (allItemsRefundInProgress) {
                order.setStatus(Order.OrderStatus.REFUND_IN_PROGRESS);
            } else {
                order.setStatus(Order.OrderStatus.PARTIALLY_REFUND_IN_PROGRESS);
            }
            orderRepository.save(order);

            RefundEvent refundEvent = RefundEvent.builder()
                    .eventId(UUID.randomUUID())
                    .orderId(order.getId())
                    .amount(refundAmount)
                    .reason("Partial cancellation by seller")
                    .build();
            orderEventProducer.publishRefundEvent(refundEvent);
        } else {
            sellerItems.forEach(item -> item.setStatus(requestedStatus));
        }

        // 2. RESTORE STOCK (only if items never shipped)
        if (!stockUpdates.isEmpty()) {
            try {
                bookClient.restoreStock(stockUpdates);
            } catch (Exception e) {
                log.error("CRITICAL: Partial stock restore failed for order {}", order.getId(), e);
            }
        }

        // 3. PARTIAL LEDGER VOID (target only this seller's items)
        List<Long> cancelledItemIds = sellerItems.stream().map(OrderItem::getId).toList();
        try {
            OrderLedgerEvent voidEvent = OrderLedgerEvent.builder()
                    .orderId(order.getId())
                    .reversal(true)
                    .orderItemIds(cancelledItemIds)
                    .build();
            orderEventProducer.publishOrderLedgerEvent(voidEvent);
        } catch (Exception e) {
            log.error("CRITICAL: Partial ledger void failed for order {}", order.getId(), e);
        }
        // NOTE: Coupon intentionally NOT restored — user still benefits on remaining items
        return itemNewStatus;
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Authentication authentication, UUID orderId) {
        UUID userId = (UUID) authentication.getPrincipal();
        Order order = getOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED
                || order.getStatus() == Order.OrderStatus.FAILED
                || order.getStatus() == Order.OrderStatus.REFUNDED) {
            return AppUtil.toOrderResponse(order);
        }

        if (order.getStatus() == Order.OrderStatus.SHIPPED
                || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new OrderCancellationException("Cannot cancel an order that is already " + order.getStatus());
        }

        // Enforce 1-week cancellation window for users
        assertCancellationWindowOpen(order);

        Order.OrderStatus newStatus = (order.getStatus() == Order.OrderStatus.CONFIRMED)
                ? Order.OrderStatus.REFUNDED
                : Order.OrderStatus.CANCELLED;

        return processFullOrderCancellation(order, newStatus);
    }

    // ===================== ADMIN - GET ALL ORDERS =====================

    @Override
    public Page<OrderResponse> getAllOrders(int pageNumber, int pageSize, String status, UUID userId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        if (status != null && userId != null) {
            return orderRepository.findByStatusAndUserId(
                            orderStatusMachine.parseStatus(status), userId, pageable)
                    .map(AppUtil::toOrderResponse);
        }
        if (status != null) {
            return orderRepository.findByStatus(
                            orderStatusMachine.parseStatus(status), pageable)
                    .map(AppUtil::toOrderResponse);
        }
        if (userId != null) {
            return orderRepository.findByUserId(userId, pageable)
                    .map(AppUtil::toOrderResponse);
        }
        return orderRepository.findAll(pageable)
                .map(AppUtil::toOrderResponse);
    }

    // ===================== ADMIN - UPDATE STATUS =====================

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, String status) {
        Order order = getOrderById(orderId);
        Order.OrderStatus newStatus = orderStatusMachine.parseStatus(status);

        // ADMIN OVERRIDE: Process full cancellation before the state machine blocks it.
        // Admins intentionally bypass the 7-day window to handle fraud/disputes on older orders.
        if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.REFUNDED) {
            return processFullOrderCancellation(order, newStatus);
        }

        // Normal status transitions (SHIPPED, DELIVERED, etc.) go through state machine
        orderStatusMachine.validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        order.getItems().forEach(item -> item.setStatus(newStatus));
        orderRepository.save(order);

        if (newStatus == Order.OrderStatus.SHIPPED && order.getEmail() != null) {
            publishShippedEmail(order, true, order.getItems());
        } else if (newStatus == Order.OrderStatus.DELIVERED && order.getEmail() != null) {
            publishDeliveredEmail(order, true, order.getItems());
        }

        return AppUtil.toOrderResponse(order);
    }

    // ===================== SELLER ENDPOINTS =====================

    @Override
    public Page<OrderResponse> getSellerOrders(Authentication authentication, int pageNumber, int pageSize, String status) {
        UUID sellerId = (UUID) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        Page<Order> ordersPage;
        if (status != null && !status.trim().isEmpty()) {
            ordersPage = orderRepository.findOrdersBySellerIdAndStatus(
                    sellerId, orderStatusMachine.parseStatus(status), pageable);
        } else {
            ordersPage = orderRepository.findOrdersBySellerIdExcludingStatuses(
                    sellerId, List.of(Order.OrderStatus.FAILED), pageable);
        }

        return ordersPage.map(order -> applySellerView(AppUtil.toOrderResponse(order), sellerId));
    }

    @Override
    @Transactional
    public OrderResponse updateSellerOrderStatus(Authentication authentication, UUID orderId, String status) {
        UUID sellerId = (UUID) authentication.getPrincipal();
        Order order = getOrderById(orderId);

        List<OrderItem> sellerItems = order.getItems().stream()
                .filter(item -> item.getSellerId() != null && item.getSellerId().equals(sellerId))
                .toList();

        if (sellerItems.isEmpty()) {
            throw new UnauthorizedOrderAccessException("You do not own any items in this order");
        }

        Order.OrderStatus newStatus = orderStatusMachine.parseStatus(status);

        boolean alreadyUpdated = sellerItems.stream().allMatch(item -> item.getStatus() == newStatus);
        if (alreadyUpdated) {
            return applySellerView(AppUtil.toOrderResponse(order), sellerId);
        }

        // SELLER CANCELLATION OVERRIDE
        if (newStatus == Order.OrderStatus.CANCELLED || newStatus == Order.OrderStatus.REFUNDED) {
            // Enforce 1-week window for sellers too
            assertCancellationWindowOpen(order);

            Order.OrderStatus finalItemStatus = processPartialOrderCancellation(order, sellerItems, newStatus);

            // Escalate parent order status only if ALL OTHER sellers' items are also cancelled/refunded
            // We check non-sellerItem IDs to avoid reading our own just-updated items
            List<Long> sellerItemIds = sellerItems.stream().map(OrderItem::getId).toList();
            boolean allOthersCancelled = order.getItems().stream()
                    .filter(i -> !sellerItemIds.contains(i.getId())) // only OTHER sellers' items
                    .allMatch(i -> i.getStatus() == Order.OrderStatus.CANCELLED
                                || i.getStatus() == Order.OrderStatus.REFUNDED
                                || i.getStatus() == Order.OrderStatus.REFUND_IN_PROGRESS);
            // Also count if there are no other items at all (single-seller order)
            boolean noOtherItems = order.getItems().stream().noneMatch(i -> !sellerItemIds.contains(i.getId()));

            if (noOtherItems || allOthersCancelled) {
                boolean allRefunding = order.getItems().stream().allMatch(i -> i.getStatus() == Order.OrderStatus.REFUND_IN_PROGRESS);
                if (allRefunding) {
                    order.setStatus(Order.OrderStatus.REFUND_IN_PROGRESS);
                } else if (order.getItems().stream().allMatch(i -> i.getStatus() == Order.OrderStatus.CANCELLED)) {
                    order.setStatus(Order.OrderStatus.CANCELLED);
                } else {
                    order.setStatus(Order.OrderStatus.PARTIALLY_CANCELLED);
                }
            }
            orderRepository.save(order);
            return applySellerView(AppUtil.toOrderResponse(order), sellerId);
        }

        // Normal fulfillment: SHIP or DELIVER
        orderStatusMachine.validateTransition(sellerItems.getFirst().getStatus(), newStatus);
        sellerItems.forEach(item -> item.setStatus(newStatus));

        // Only consider ACTIVE items (ignore items that were partially cancelled by this or another seller)
        List<OrderItem> activeItems = order.getItems().stream()
                .filter(item -> item.getStatus() != Order.OrderStatus.CANCELLED
                             && item.getStatus() != Order.OrderStatus.REFUNDED
                             && item.getStatus() != Order.OrderStatus.REFUND_IN_PROGRESS
                             && item.getStatus() != Order.OrderStatus.PARTIALLY_REFUND_IN_PROGRESS)
                .toList();

        boolean allItemsShipped   = !activeItems.isEmpty() && activeItems.stream().allMatch(item -> item.getStatus() == Order.OrderStatus.SHIPPED || item.getStatus() == Order.OrderStatus.DELIVERED);
        boolean allItemsDelivered = !activeItems.isEmpty() && activeItems.stream().allMatch(item -> item.getStatus() == Order.OrderStatus.DELIVERED);

        boolean isFinalShipment = false;
        boolean isFinalDelivery = false;

        if (newStatus == Order.OrderStatus.SHIPPED && allItemsShipped && order.getStatus() != Order.OrderStatus.SHIPPED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            order.setStatus(Order.OrderStatus.SHIPPED);
            isFinalShipment = true;
        } else if (newStatus == Order.OrderStatus.DELIVERED && allItemsDelivered && order.getStatus() != Order.OrderStatus.DELIVERED) {
            order.setStatus(Order.OrderStatus.DELIVERED);
            isFinalDelivery = true;
        }

        orderRepository.save(order);

        if (newStatus == Order.OrderStatus.SHIPPED && order.getEmail() != null) {
            publishShippedEmail(order, isFinalShipment, sellerItems);
        } else if (newStatus == Order.OrderStatus.DELIVERED && order.getEmail() != null) {
            publishDeliveredEmail(order, isFinalDelivery, sellerItems);
        }

        return applySellerView(AppUtil.toOrderResponse(order), sellerId);
    }

    // ===================== PRIVATE HELPERS =====================

    private void publishSellerNotifications(Order order) {
        if (order.getItems() == null) return;

        Map<String, List<OrderItem>> itemsBySeller = order.getItems().stream()
                .filter(item -> item.getSellerEmail() != null && !item.getSellerEmail().trim().isEmpty())
                .collect(Collectors.groupingBy(OrderItem::getSellerEmail));

        itemsBySeller.forEach((sellerEmail, sellerItems) -> {
            try {
                orderEventProducer.publishSellerOrderEvent(
                        orderEventMapper.buildSellerEvent(order, sellerEmail, sellerItems));
            } catch (Exception e) {
                log.error("Failed to publish SellerOrderEvent for seller {}", sellerEmail, e);
            }
        });
    }

    private void publishShippedEmail(Order order, boolean isFinalShipment, List<OrderItem> newlyShipped) {
        try {
            orderEventProducer.publishOrderShippedEvent(orderEventMapper.buildShippedEvent(order, isFinalShipment, newlyShipped));
        } catch (Exception e) {
            log.error("Failed to publish OrderShippedEvent for order {}", order.getId(), e);
        }
    }

    private void publishDeliveredEmail(Order order, boolean isFinalDelivery, List<OrderItem> newlyDelivered) {
        try {
            orderEventProducer.publishOrderDeliveredEvent(orderEventMapper.buildDeliveredEvent(order, isFinalDelivery, newlyDelivered));
        } catch (Exception e) {
            log.error("Failed to publish OrderDeliveredEvent for order {}", order.getId(), e);
        }
    }

    private OrderResponse applySellerView(OrderResponse response, UUID sellerId) {
        List<com.bookheaven.order_service.dto.OrderResponseDto.OrderItemResponse> filteredItems =
                response.getItems().stream()
                        .filter(item -> item.getSellerId() != null && item.getSellerId().equals(sellerId.toString()))
                        .toList();
        response.setItems(filteredItems);
        double sellerTotal = filteredItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        response.setTotalAmount(sellerTotal);
        return response;
    }

    private String generateUniqueOrderReference() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        java.security.SecureRandom random = new java.security.SecureRandom();
        String ref;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            ref = sb.toString();
        } while (orderRepository.existsByOrderReference(ref));
        return ref;
    }
}
