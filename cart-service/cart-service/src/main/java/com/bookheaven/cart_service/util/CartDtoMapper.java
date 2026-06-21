package com.bookheaven.cart_service.util;

import com.bookheaven.cart_service.dto.cartResponseDto.BookDto;
import com.bookheaven.cart_service.dto.cartResponseDto.CartItemResponse;
import com.bookheaven.cart_service.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CartDtoMapper {

    public List<CartItemResponse> mapToItemResponses(List<CartItem> cartItems, List<BookDto> books) {
        Map<Long, BookDto> bookMap = books.stream()
                .collect(Collectors.toMap(BookDto::getListingId, b -> b));

        return cartItems.stream()
                .map(item -> {
                    BookDto book = bookMap.get(item.getListingId());
                    if (book == null) return null; // listing deleted case
                    return CartItemResponse.builder()
                            .listingId(book.getListingId())
                            .bookId(book.getBookId())
                            .title(book.getTitle())
                            .author(book.getAuthor())
                            .imageUrl(book.getImg())
                            .price(book.getPrice())
                            .currency(book.getCurrency())
                            .quantity(item.getQuantity())
                            .available(book.getCopiesAvailable())
                            .sellerId(book.getSellerId())
                            .sellerUsername(book.getSellerUsername())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
