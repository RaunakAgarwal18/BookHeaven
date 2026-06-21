package com.bookheaven.review_service.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponse {
    Long id;
    String username;
    String reviewDescription;
    LocalDateTime createdAt;
    Double rating;
    List<String> photos;
    int upvoteCount;
    boolean upvotedByCurrentUser;
}
