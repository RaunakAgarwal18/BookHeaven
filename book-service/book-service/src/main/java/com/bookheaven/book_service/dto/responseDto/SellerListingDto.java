package com.bookheaven.book_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerListingDto {
    private Long listingId;
    private String sellerUsername;
    private Double price;
    private String currency;
    private int copiesAvailable;
}
