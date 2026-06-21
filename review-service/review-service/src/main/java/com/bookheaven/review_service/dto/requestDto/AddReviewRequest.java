package com.bookheaven.review_service.dto.requestDto;

import lombok.Data;
import java.util.List;

@Data
public class AddReviewRequest {
    private double rating;
    private Long bookId;
    private String reviewDescription;
    private List<String> photos;
}
