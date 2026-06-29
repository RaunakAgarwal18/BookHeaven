package com.bookheaven.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockUpdateRequest {
    private Long bookId;
    private Integer quantity;
}
