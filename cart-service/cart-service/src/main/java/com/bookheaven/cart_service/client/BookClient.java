package com.bookheaven.cart_service.client;

import com.bookheaven.common.dto.response.BookDto;
import java.util.List;

public interface BookClient {
    List<BookDto> getBooksByIds(List<Long> bookIds);
}
