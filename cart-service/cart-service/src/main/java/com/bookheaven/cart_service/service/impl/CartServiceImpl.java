package com.bookheaven.cart_service.service.impl;

import com.bookheaven.cart_service.dto.cartResponseDto.BookDto;
import com.bookheaven.cart_service.dto.cartResponseDto.CartItemResponse;
import com.bookheaven.cart_service.dto.cartResponseDto.CartResponse;
import com.bookheaven.cart_service.entity.Cart;
import com.bookheaven.cart_service.entity.CartItem;
import com.bookheaven.cart_service.exception.BookServiceException;
import com.bookheaven.cart_service.exception.CartItemNotFoundException;
import com.bookheaven.cart_service.repository.CartRepository;
import com.bookheaven.cart_service.util.CartDtoMapper;
import com.bookheaven.cart_service.service.CartPriceCalculator;
import com.bookheaven.cart_service.client.BookClient;
import com.bookheaven.cart_service.service.CartService;
import com.bookheaven.cart_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final BookClient bookClient;
    private final CartDtoMapper cartDtoMapper;
    private final CartPriceCalculator cartPriceCalculator;
    private final CouponService couponService;

    // ===================== GET CART =====================
    public CartResponse getCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createCart(userId));
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(UUID userId, Long listingId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createCart(userId));
        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(i -> i.getListingId().equals(listingId))
                .findFirst();
        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            CartItem item = CartItem.builder()
                    .listingId(listingId)
                    .quantity(quantity)
                    .build();
            cart.addItem(item);
        }
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public CartResponse updateQuantity(UUID userId, Long listingId, int quantity) {
        Cart cart = getExistingCart(userId);
        CartItem item = cart.getItems()
                .stream()
                .filter(i -> i.getListingId().equals(listingId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart for listingId: " + listingId));
        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.updateQuantity(quantity);
        }
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public CartResponse removeFromCart(UUID userId, Long listingId) {
        Cart cart = getExistingCart(userId);
        CartItem item = cart.getItems()
                .stream()
                .filter(i -> i.getListingId().equals(listingId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Item not found in cart for listingId: " + listingId));
        cart.removeItem(item);
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public void clearCart(UUID userId) {
        Cart cart = getExistingCart(userId);
        cart.getItems().clear(); // orphanRemoval → DB delete
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);
    }

    public CartResponse applyCoupon(UUID userId, String couponCode) {
        Cart cart = getExistingCart(userId);
        CartResponse currentCart = buildCartResponse(cart);
        
        // Validate coupon with current subtotal
        couponService.validateCoupon(couponCode, currentCart.getTotalAmount());
        
        cart.setAppliedCouponCode(couponCode.toUpperCase());
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    public CartResponse removeCoupon(UUID userId) {
        Cart cart = getExistingCart(userId);
        cart.setAppliedCouponCode(null);
        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    private Cart getExistingCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    private Cart createCart(UUID userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());
        if (cartItems.isEmpty()) {
            return CartResponse.builder()
                    .userId(cart.getUserId())
                    .items(List.of())
                    .taxAmount(0.0)
                    .shippingAmount(0.0)
                    .totalAmount(0.0)
                    .currency("INR")
                    .build();
        }
        
        List<Long> listingIds = cartItems.stream()
                .map(CartItem::getListingId)
                .distinct()
                .toList();

        List<BookDto> books;
        try {
            books = bookClient.getBooksByIds(listingIds);
        } catch (Exception ex) {
            throw new BookServiceException("Failed to fetch book details from Book Service: " + ex.getMessage());
        }

        List<CartItemResponse> items = cartDtoMapper.mapToItemResponses(cartItems, books);
        CartPriceCalculator.PricingResult pricing = cartPriceCalculator.calculate(cart, items);
        
        if (pricing.invalidCoupon()) {
            cart.setAppliedCouponCode(null);
            cartRepository.save(cart);
        }
        
        String currency = items.isEmpty() ? "INR" : items.getFirst().getCurrency();

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(items)
                .subtotal(pricing.subtotal())
                .couponCode(pricing.couponCode())
                .discountAmount(pricing.discountAmount())
                .discountSellerId(pricing.discountSellerId())
                .taxAmount(pricing.taxAmount())
                .shippingAmount(pricing.shippingAmount())
                .totalAmount(pricing.finalTotal())
                .currency(currency)
                .build();
    }
}