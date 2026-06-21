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
@Table(name = "seller_ledger", indexes = {
        @Index(name = "idx_ledger_seller_id", columnList = "seller_id"),
        @Index(name = "idx_ledger_order_id", columnList = "order_id"),
        @Index(name = "idx_ledger_status", columnList = "settlement_status"),
        @Index(name = "idx_ledger_settlement_id", columnList = "settlement_id")
})
public class SellerLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "seller_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID sellerId;

    @Column(name = "gross_amount", nullable = false)
    private Double grossAmount;

    @Column(name = "seller_discount", nullable = false)
    @Builder.Default
    private Double sellerDiscount = 0.0;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate;

    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount;

    @Column(name = "net_payout", nullable = false)
    private Double netPayout;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    private SettlementStatus settlementStatus;

    @Column(name = "settlement_id", columnDefinition = "VARCHAR(36)")
    private UUID settlementId;

    @Column(name = "gateway_transfer_id", length = 100)
    private String gatewayTransferId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SettlementStatus {
        PENDING,
        PROCESSING,
        SETTLED,
        FAILED
    }
}
