package com.bookheaven.review_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_book_id", columnList = "book_id"),
        @Index(name = "idx_review_user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_user_book", columnNames = {"user_id", "book_id"})
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID userId;

    @NotBlank
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Min(1) @Max(5)
    @Column(name = "rating", nullable = false)
    private double rating;

    @NotNull
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Size(max = 5000)
    @Column(name = "review_description", length = 5000)
    private String reviewDescription;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_photos",
            joinColumns = @JoinColumn(name = "review_id", foreignKey = @ForeignKey(name = "fk_review_photos_review_id"))
    )
    @Column(name = "photo", columnDefinition = "MEDIUMTEXT")
    @Builder.Default
    private List<String> photos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_upvotes",
            joinColumns = @JoinColumn(name = "review_id", foreignKey = @ForeignKey(name = "fk_review_upvotes_review_id")),
            uniqueConstraints = @UniqueConstraint(name = "uq_review_upvote", columnNames = {"review_id", "user_id"})
    )
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)")
    @Builder.Default
    private Set<UUID> upvotedUserIds = new HashSet<>();

    @Version
    @Column(name = "version")
    private Long version;
}