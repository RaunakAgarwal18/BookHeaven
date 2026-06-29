package com.bookheaven.cart_service.service;

import com.bookheaven.common.dto.response.CartItemResponse;
import com.bookheaven.cart_service.dto.cartResponseDto.CouponResponse;
import com.bookheaven.cart_service.entity.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartPriceCalculator {

    private final CouponService couponService;

    @Value("${pricing.tax.rate:0.18}")
    private double taxRate;

    @Value("${pricing.shipping.rate:0.06}")
    private double shippingRate;

    public PricingResult calculate(Cart cart, List<CartItemResponse> items) {
        double subtotal = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
                
        double discountAmount = 0.0;
        UUID discountSellerId = null;
        String appliedCoupon = cart.getAppliedCouponCode();
        boolean invalidCoupon = false;
        
        if (appliedCoupon != null && subtotal > 0) {
            try {
                CouponResponse coupon = couponService.getCouponByCode(appliedCoupon);
                double eligibleSubtotal = subtotal;
                
                if (coupon.getSellerId() != null) {
                    eligibleSubtotal = items.stream()
                        .filter(i -> coupon.getSellerId().equals(i.getSellerId()))
                        .mapToDouble(i -> i.getPrice() * i.getQuantity())
                        .sum();
                        
                    if (eligibleSubtotal <= 0) {
                        throw new RuntimeException("No eligible items for this seller's coupon.");
                    }
                    discountSellerId = coupon.getSellerId();
                }
                
                couponService.validateCoupon(appliedCoupon, eligibleSubtotal);
                
                if (coupon.getDiscountType() == com.bookheaven.cart_service.entity.DiscountType.PERCENTAGE) {
                    discountAmount = eligibleSubtotal * (coupon.getDiscountValue() / 100.0);
                    if (coupon.getMaxDiscountAmount() != null && discountAmount > coupon.getMaxDiscountAmount()) {
                        discountAmount = coupon.getMaxDiscountAmount();
                    }
                } else if (coupon.getDiscountType() == com.bookheaven.cart_service.entity.DiscountType.FLAT) {
                    discountAmount = coupon.getDiscountValue();
                }
                
                if (discountAmount > eligibleSubtotal) {
                    discountAmount = eligibleSubtotal;
                }
            } catch (Exception e) {
                log.warn("Cart's applied coupon {} became invalid: {}", appliedCoupon, e.getMessage());
                invalidCoupon = true;
                appliedCoupon = null;
                discountAmount = 0.0;
                discountSellerId = null;
            }
        }
        
        double taxAmount = subtotal * taxRate;
        double shippingAmount = subtotal * shippingRate;
        double finalTotal = Math.round(((subtotal - discountAmount) + taxAmount + shippingAmount) * 100.0) / 100.0;

        return new PricingResult(
            subtotal, 
            appliedCoupon, 
            discountAmount, 
            discountSellerId, 
            taxAmount, 
            shippingAmount, 
            finalTotal, 
            invalidCoupon
        );
    }

    public record PricingResult(
        double subtotal,
        String couponCode,
        double discountAmount,
        UUID discountSellerId,
        double taxAmount,
        double shippingAmount,
        double finalTotal,
        boolean invalidCoupon
    ) {}
}
