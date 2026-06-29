package com.bookheaven.common.dto.response;

import lombok.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookPublicResponse {
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
