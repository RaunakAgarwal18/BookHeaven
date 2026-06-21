package com.bookheaven.book_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class BookSellerResponse {
    private Long listingId;
    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String img;
    private Double price;
    private String currency;
    private int copies;
    private int copiesAvailable;
    private UUID sellerId;
    private String sellerUsername;
}
