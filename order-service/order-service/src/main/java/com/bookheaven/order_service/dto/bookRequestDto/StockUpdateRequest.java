package com.bookheaven.order_service.dto.bookRequestDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockUpdateRequest {
    private Long bookId;
    private Integer quantity;
}