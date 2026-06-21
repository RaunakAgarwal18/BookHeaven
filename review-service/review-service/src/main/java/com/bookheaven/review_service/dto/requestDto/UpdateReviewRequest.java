package com.bookheaven.review_service.dto.requestDto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateReviewRequest {
    private double rating;
    private String reviewDescription;
    private List<String> photos;
}
