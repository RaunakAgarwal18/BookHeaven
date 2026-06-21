package com.bookheaven.book_service.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockUpdateRequest {
    private Long bookId;
    private Integer quantity;
}