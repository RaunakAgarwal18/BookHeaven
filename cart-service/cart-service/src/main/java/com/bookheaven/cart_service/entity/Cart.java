package com.bookheaven.cart_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "carts",
        indexes = {
                @Index(name = "idx_cart_user_id", columnList = "user_id", unique = true)
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID cartId;

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "VARCHAR(36)")
    private UUID userId;

    @Builder.Default
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<CartItem> items = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "applied_coupon_code", length = 50)
    private String appliedCouponCode;

    @Version
    @Column(name = "version")
    private long version;

    // Helper methods (important for consistency)
    public void addItem(CartItem item) {
        item.setCart(this);
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        item.setCart(null);
        this.items.remove(item);
    }
}