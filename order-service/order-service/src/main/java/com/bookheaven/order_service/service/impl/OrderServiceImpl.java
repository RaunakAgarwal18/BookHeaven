package com.bookheaven.order_service.service.impl;

import com.bookheaven.order_service.dto.Event.*;
import com.bookheaven.order_service.dto.OrderResponseDto.OrderResponse;
import com.bookheaven.order_service.dto.bookRequestDto.BookDto;
import com.bookheaven.order_service.dto.bookRequestDto.StockUpdateRequest;
import com.bookheaven.order_service.dto.cartResponseDto.CartItemResponse;
import com.bookheaven.order_service.dto.cartResponseDto.CartResponse;
import com.bookheaven.order_service.dto.checkoutRequestDto.CheckoutRequest;
import com.bookheaven.order_service.dto.checkoutResponseDto.CheckoutResponse;
import com.bookheaven.order_service.dto.paymentResponseDto.InitiatePaymentResponse;
import com.bookheaven.order_service.dto.userResponseDto.AddressResponse;
import com.bookheaven.order_service.dto.userResponseDto.UserResponse;
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
        Order savedOrder = orderRepository.save(order);
        orderEventProducer.publishOrderTimeoutEvent(new OrderTimeoutEvent(savedOrder.getId()));

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

        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.CONFIRMED));
        order.setPaymentId(gatewayPaymentId);
        orderRepository.save(order);

        // Reduce stock
        List<StockUpdateRequest> stockUpdates = order.getItems().stream()
                .map(item -> new StockUpdateRequest(item.getListingId(), item.getQuantity()))
                .toList();
        try {
            bookClient.reduceStock(stockUpdates);
        } catch (Exception e) {
            throw new BookServiceException("Failed to reduce stock");
        }

        try {
            cartClient.clearCart(order.getUserId());
            if (order.getCouponCode() != null) {
                cartClient.incrementCouponUsage(order.getCouponCode());
            }
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
        order.setStatus(Order.OrderStatus.FAILED);
        order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.FAILED));
        orderRepository.save(order);
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

    @Override
    @Transactional
    public OrderResponse cancelOrder(Authentication authentication, UUID orderId) {
        UUID userId = (UUID) authentication.getPrincipal();

        Order order = getOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new OrderCancellationException("Cannot cancel order that is already " + order.getStatus());
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            return AppUtil.toOrderResponse(order);
        }

        // Restore stock
        List<StockUpdateRequest> stockUpdates = order.getItems().stream()
                .map(item -> new StockUpdateRequest(item.getListingId(), item.getQuantity()))
                .toList();
        try {
            bookClient.restoreStock(stockUpdates);
        } catch (Exception e) {
            throw new BookServiceException("Failed to restore stock");
        }

        // Initiate refund if payment was made
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            try {
                paymentClient.refund(order.getId(), order.getTotalAmount(), "Order cancelled by user");
            } catch (Exception e) {
                throw new PaymentServiceException("Failed to initiate refund");
            }
            order.setStatus(Order.OrderStatus.REFUNDED);
            order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.REFUNDED));
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.getItems().forEach(item -> item.setStatus(Order.OrderStatus.CANCELLED));
        }

        orderRepository.save(order);
        return AppUtil.toOrderResponse(order);
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

        orderStatusMachine.validateTransition(sellerItems.get(0).getStatus(), newStatus);
        sellerItems.forEach(item -> item.setStatus(newStatus));

        boolean allItemsShipped = order.getItems().stream().allMatch(item -> item.getStatus() == Order.OrderStatus.SHIPPED || item.getStatus() == Order.OrderStatus.DELIVERED);
        boolean allItemsDelivered = order.getItems().stream().allMatch(item -> item.getStatus() == Order.OrderStatus.DELIVERED);

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