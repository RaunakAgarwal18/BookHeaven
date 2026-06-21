package com.bookheaven.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
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
