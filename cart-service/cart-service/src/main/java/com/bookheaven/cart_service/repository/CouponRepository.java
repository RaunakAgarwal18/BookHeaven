package com.bookheaven.cart_service.repository;

import com.bookheaven.cart_service.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findByCode(String code);
    
    @Modifying
    @Query("UPDATE Coupon c SET c.isActive = false WHERE c.expiryDate < :now AND c.isActive = true")
    int deactivateExpiredCoupons(LocalDateTime now);
}
