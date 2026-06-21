package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.dto.BookDto;
import com.bookheaven.user_service.entity.User;
import com.bookheaven.user_service.service.UserService;
import com.bookheaven.user_service.service.WishlistService;
import com.bookheaven.user_service.service.clientService.BookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final UserService userService;
    private final BookClient bookClient;

    @Override
    public void addWishlist(Authentication authentication, Long bookId) {
        User user = userService.getSelfUser(authentication);
        user.getWishlist().add(bookId);
        userService.saveUser(user);
    }

    @Override
    public void removeWishlist(Authentication authentication, Long bookId) {
        User user = userService.getSelfUser(authentication);
        user.getWishlist().remove(bookId);
        userService.saveUser(user);
    }

    @Override
    public Set<Long> getWishlistIds(Authentication authentication) {
        User user = userService.getSelfUser(authentication);
        return user.getWishlist();
    }

    @Override
    public List<BookDto> getWishlistDetails(Authentication authentication) {
        User user = userService.getSelfUser(authentication);
        List<Long> bookIds = new ArrayList<>(user.getWishlist());
        return bookClient.getBulkBooks(bookIds);
    }
}
