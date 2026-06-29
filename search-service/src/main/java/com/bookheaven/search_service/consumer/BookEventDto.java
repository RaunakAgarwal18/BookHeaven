package com.bookheaven.search_service.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEventDto {
    private Long bookId;
    private String title;
    private String author;
    private String description;
    private String category;
    private String img;
    private String isbn;
    private Long cheapestListingId;
    private Double lowestPrice;
    private String lowestCurrency;
    private int totalCopiesAvailable;
    private Double averageRating;
    private Integer totalReviews;
}
