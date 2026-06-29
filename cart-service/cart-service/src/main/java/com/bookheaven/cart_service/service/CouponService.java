package com.bookheaven.cart_service.service;

import com.bookheaven.cart_service.dto.cartResponseDto.CouponResponse;
import com.bookheaven.cart_service.dto.requestDto.CouponRequest;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request, Authentication authentication);
    CouponResponse getCouponByCode(String code);
    List<CouponResponse> getAllCoupons(Authentication authentication);
    CouponResponse toggleCouponStatus(UUID id, Authentication authentication);
    void validateCoupon(String code, Double subtotal);
    void incrementUsage(String code);
    void decrementUsage(String code);
    void deleteCoupon(UUID id, Authentication authentication);
}
