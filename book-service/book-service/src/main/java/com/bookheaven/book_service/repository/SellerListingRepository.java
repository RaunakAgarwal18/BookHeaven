package com.bookheaven.book_service.repository;

import com.bookheaven.book_service.entity.SellerListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerListingRepository extends JpaRepository<SellerListing, Long> {

    List<SellerListing> findByBookIdOrderByPriceAsc(Long bookId);

    List<SellerListing> findByBookIdIn(List<Long> bookIds);

    Optional<SellerListing> findBySellerIdAndBookId(UUID sellerId, Long bookId);

    boolean existsBySellerIdAndBookId(UUID sellerId, Long bookId);

    Page<SellerListing> findBySellerId(UUID sellerId, Pageable pageable);

    @Query("SELECT sl FROM SellerListing sl WHERE sl.book.id = :bookId AND sl.copiesAvailable > 0 ORDER BY sl.price ASC")
    List<SellerListing> findAvailableByBookIdOrderByPriceAsc(Long bookId);
}
