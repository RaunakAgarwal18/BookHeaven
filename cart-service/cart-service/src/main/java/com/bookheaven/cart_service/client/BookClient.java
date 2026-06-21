package com.bookheaven.cart_service.client;

import com.bookheaven.cart_service.dto.cartResponseDto.BookDto;
import java.util.List;

public interface BookClient {
    List<BookDto> getBooksByIds(List<Long> bookIds);
}
