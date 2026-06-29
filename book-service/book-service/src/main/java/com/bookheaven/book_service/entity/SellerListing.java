package com.bookheaven.book_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "seller_listings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_seller_book", columnNames = {"book_id", "seller_id"})
        },
        indexes = {
                @Index(name = "idx_listing_seller_id", columnList = "seller_id"),
                @Index(name = "idx_listing_book_id", columnList = "book_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- BOOK FK ----------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_listing_book"))
    private Book book;

    // ---------------- SELLER INFO ----------------

    @Column(name = "seller_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID sellerId;

    @Column(name = "seller_username", nullable = false, length = 100)
    private String sellerUsername;

    @Column(name = "seller_email", nullable = false, length = 150)
    private String sellerEmail;

    // ---------------- PRICING ----------------

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false)
    private Double price;

    @Pattern(regexp = "^[A-Z]{3}$")
    @Column(nullable = false, length = 3)
    private String currency;

    // ---------------- INVENTORY ----------------

    @Min(0)
    @Column(nullable = false)
    private int copies;

    @Min(0)
    @Column(name = "copies_available", nullable = false)
    private int copiesAvailable;

    // ---------------- AUDIT ----------------

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------------- BUSINESS METHODS ----------------

    public void increaseCopies(int qty) {
        this.copies += qty;
        this.copiesAvailable += qty;
    }

    public void decreaseAvailable(int qty) {
        if (this.copiesAvailable < qty) {
            throw new IllegalArgumentException("Not enough copies available");
        }
        this.copiesAvailable -= qty;
    }

    public void increaseAvailable(int qty) {
        this.copiesAvailable += qty;
    }
}
