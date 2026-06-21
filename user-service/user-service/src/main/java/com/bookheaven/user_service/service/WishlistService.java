package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.BookDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

public interface WishlistService {
    void addWishlist(Authentication authentication, Long bookId);
    void removeWishlist(Authentication authentication, Long bookId);
    Set<Long> getWishlistIds(Authentication authentication);
    List<BookDto> getWishlistDetails(Authentication authentication);
}
