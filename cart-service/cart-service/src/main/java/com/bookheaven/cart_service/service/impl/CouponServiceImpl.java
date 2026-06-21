package com.bookheaven.cart_service.service.impl;

import com.bookheaven.cart_service.dto.cartResponseDto.CouponResponse;
import com.bookheaven.cart_service.dto.requestDto.CouponRequest;
import com.bookheaven.cart_service.entity.Coupon;
import com.bookheaven.cart_service.entity.CouponStatus;
import com.bookheaven.cart_service.exception.InvalidCouponException;
import com.bookheaven.cart_service.repository.CouponRepository;
import com.bookheaven.cart_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request, Authentication authentication) {
        if (couponRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new InvalidCouponException("Coupon code already exists");
        }

        UUID sellerId = request.getSellerId();
        if (authentication != null) {
            boolean isSeller = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_SELLER"));
            if (isSeller) {
                sellerId = (UUID) authentication.getPrincipal();
            }
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .sellerId(sellerId)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .isActive(true)
                .startDate(request.getStartDate())
                .expiryDate(request.getExpiryDate())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .build();

        return toResponse(couponRepository.save(coupon));
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new InvalidCouponException("Coupon not found"));
        return toResponse(coupon);
    }

    @Override
    public List<CouponResponse> getAllCoupons(Authentication authentication) {
        List<Coupon> coupons = couponRepository.findAll();
        
        if (authentication != null) {
            boolean isSeller = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_SELLER"));
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                coupons = coupons.stream()
                        .filter(c -> sellerId.equals(c.getSellerId()))
                        .collect(Collectors.toList());
            }
        }
        
        return coupons.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CouponResponse toggleCouponStatus(UUID id, Authentication authentication) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new InvalidCouponException("Coupon not found"));
                
        if (authentication != null) {
            boolean isSeller = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_SELLER"));
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                if (!sellerId.equals(coupon.getSellerId())) {
                    throw new InvalidCouponException("You are not authorized to toggle this coupon");
                }
            }
        }
                
        if (LocalDateTime.now().isAfter(coupon.getExpiryDate()) || coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("Cannot toggle an expired coupon");
        }
                
        coupon.setIsActive(!coupon.getIsActive());
        return toResponse(couponRepository.save(coupon));
    }

    @Override
    public void validateCoupon(String code, Double subtotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new InvalidCouponException("Invalid coupon code"));

        CouponStatus status = computeStatus(coupon);
        if (status == CouponStatus.EXPIRED) {
            throw new InvalidCouponException("This coupon has expired or reached its usage limit");
        }
        if (status == CouponStatus.INACTIVE) {
            throw new InvalidCouponException("This coupon is no longer active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("This coupon is not yet valid");
        }

        if (coupon.getMinOrderAmount() != null && subtotal < coupon.getMinOrderAmount()) {
            throw new InvalidCouponException("Minimum order amount of " + coupon.getMinOrderAmount() + " is required to use this coupon");
        }
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new InvalidCouponException("Coupon not found"));
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
    }



    @Override
    @Transactional
    public void deleteCoupon(UUID id, Authentication authentication) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new InvalidCouponException("Coupon not found"));

        if (authentication != null) {
            boolean isSeller = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_SELLER"));
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                if (!sellerId.equals(coupon.getSellerId())) {
                    throw new InvalidCouponException("You are not authorized to delete this coupon");
                }
            }
        }

        couponRepository.delete(coupon);
        log.info("Deleted coupon: {} (id: {})", coupon.getCode(), id);
    }

    private CouponStatus computeStatus(Coupon coupon) {
        if (LocalDateTime.now().isAfter(coupon.getExpiryDate()) || coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return CouponStatus.EXPIRED;
        }
        return coupon.getIsActive() ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;
    }

    private CouponResponse toResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .sellerId(coupon.getSellerId())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .status(computeStatus(coupon))
                .startDate(coupon.getStartDate())
                .expiryDate(coupon.getExpiryDate())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .build();
    }
}
