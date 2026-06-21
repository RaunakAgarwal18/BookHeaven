package com.bookheaven.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id"),
        @Index(name = "idx_order_items_listing_id", columnList = "listing_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order_id"))
    private Order order;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "seller_id", columnDefinition = "VARCHAR(36)")
    private UUID sellerId;

    @Column(name = "seller_username", length = 100)
    private String sellerUsername;

    @Column(name = "seller_email", length = 150)
    private String sellerEmail;
}