package com.bookheaven.book_service.dto.requestDto;

import lombok.Data;

@Data
public class UpdateListingRequest {
    private Integer copies;
    private Double price;
    private String currency;
}
