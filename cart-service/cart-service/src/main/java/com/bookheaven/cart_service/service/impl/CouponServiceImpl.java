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
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
                    
            if (isSeller) {
                sellerId = (UUID) authentication.getPrincipal();
            } else if (!isAdmin) {
                throw new InvalidCouponException("Only sellers or admins can create coupons");
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
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
                    
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                coupons = coupons.stream()
                        .filter(c -> sellerId.equals(c.getSellerId()))
                        .collect(Collectors.toList());
            } else if (!isAdmin) {
                // Regular users should only see active, non-expired coupons
                coupons = coupons.stream()
                        .filter(c -> c.getIsActive() && java.time.LocalDateTime.now().isBefore(c.getExpiryDate()))
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
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
                    
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                if (!sellerId.equals(coupon.getSellerId())) {
                    throw new InvalidCouponException("You are not authorized to toggle this coupon");
                }
            } else if (!isAdmin) {
                throw new InvalidCouponException("You are not authorized to toggle this coupon");
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
                .orElseThrow(() -> new InvalidCouponException("Invalid coupon"));

        CouponStatus status = computeStatus(coupon);
        if (status == CouponStatus.EXPIRED) {
            throw new InvalidCouponException("Coupon Expired");
        }
        if (status == CouponStatus.INACTIVE) {
            throw new InvalidCouponException("Invalid coupon");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("Invalid coupon");
        }

        if (coupon.getMinOrderAmount() != null && subtotal < coupon.getMinOrderAmount()) {
            double shortfall = coupon.getMinOrderAmount() - subtotal;
            throw new InvalidCouponException(String.format("Add %.2f more to unlock coupon", shortfall));
        }
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        int updatedRows = couponRepository.incrementUsageAtomic(code.toUpperCase());
        if (updatedRows == 0) {
            throw new InvalidCouponException("Coupon not found, inactive, or usage limit reached");
        }
    }

    @Override
    @Transactional
    public void decrementUsage(String code) {
        couponRepository.decrementUsageAtomic(code.toUpperCase());
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
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
                    
            if (isSeller) {
                UUID sellerId = (UUID) authentication.getPrincipal();
                if (!sellerId.equals(coupon.getSellerId())) {
                    throw new InvalidCouponException("You are not authorized to delete this coupon");
                }
            } else if (!isAdmin) {
                throw new InvalidCouponException("You are not authorized to delete this coupon");
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
