package com.bookheaven.book_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookPublicResponse {
    private Long bookId;
    private String title;
    private String author;
    private String description;
    private String category;
    private String img;
    private String isbn;
    private Double lowestPrice;
    private String lowestCurrency;
    private int totalCopiesAvailable;
    private Double averageRating;
    private Integer totalReviews;
}
