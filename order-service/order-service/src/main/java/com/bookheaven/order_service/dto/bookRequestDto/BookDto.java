package com.bookheaven.order_service.dto.bookRequestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long listingId;
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Double price;
    private String currency;
    private int copiesAvailable;
    private UUID sellerId;
    private String sellerUsername;
    private String sellerEmail;
}
