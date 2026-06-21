package com.bookheaven.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_user_id", columnList = "user_id"),
        @Index(name = "idx_payments_gateway_order_id", columnList = "gateway_order_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID orderId;

    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID userId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider", nullable = true, length = 50)
    private String provider; // e.g. "razorpay"

    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;      // Razorpay order_id

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;    // Razorpay payment_id (set after payment)

    @Column(name = "gateway_signature", length = 255)
    private String gatewaySignature;    // Razorpay signature (set after verification)

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;       // UPI, CARD, NETBANKING etc

    @Column(name = "failure_reason", length = 255)
    private String failureReason;       // set if payment failed

    @Column(name = "refund_id", length = 100)
    private String refundId;            // Razorpay refund_id (set after refund)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        REFUNDED
    }
}
