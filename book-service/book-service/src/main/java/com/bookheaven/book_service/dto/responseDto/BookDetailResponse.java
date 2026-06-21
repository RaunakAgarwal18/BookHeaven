package com.bookheaven.book_service.dto.responseDto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookDetailResponse {
    private Long bookId;
    private String title;
    private String author;
    private String description;
    private String category;
    private String img;
    private String isbn;
    private Double averageRating;
    private Integer totalReviews;
    private List<SellerListingDto> listings;
}
