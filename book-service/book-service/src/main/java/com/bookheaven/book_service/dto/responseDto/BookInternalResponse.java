package com.bookheaven.book_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class BookInternalResponse {
    private Long listingId;
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private String img;
    private Double price;
    private String currency;
    private int copiesAvailable;
    private UUID sellerId;
    private String sellerUsername;
    private String sellerEmail;
}
