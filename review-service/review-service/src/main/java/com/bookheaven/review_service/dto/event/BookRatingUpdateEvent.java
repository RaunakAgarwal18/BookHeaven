package com.bookheaven.review_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRatingUpdateEvent {
    private Long bookId;
    private Double averageRating;
    private Integer totalReviews;
}
