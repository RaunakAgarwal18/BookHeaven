package com.bookheaven.cart_service.dto.requestDto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @Min(0)
    private Integer quantity;
}