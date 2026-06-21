package com.bookheaven.cart_service.controller;

import com.bookheaven.cart_service.dto.cartResponseDto.CouponResponse;
import com.bookheaven.cart_service.dto.requestDto.CouponRequest;
import com.bookheaven.cart_service.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons(Authentication authentication) {
        return ResponseEntity.ok(couponService.getAllCoupons(authentication));
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CouponResponse> toggleCouponStatus(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(couponService.toggleCouponStatus(id, authentication));
    }

    @PostMapping("/{code}/increment-usage")
    public ResponseEntity<Void> incrementUsage(@PathVariable String code) {
        couponService.incrementUsage(code);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID id, Authentication authentication) {
        couponService.deleteCoupon(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
