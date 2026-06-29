package com.bookheaven.common.dto.response;

import lombok.*;

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
    private String img;
    private Double price;
    private String currency;
    private int copiesAvailable;
    private java.util.UUID sellerId;
    private String sellerUsername;
    private String sellerEmail;
}
