package com.bookheaven.review_service.util;

import com.bookheaven.review_service.dto.ResponseDto.ReviewResponse;
import com.bookheaven.review_service.entity.Review;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.UUID;

public class AppUtil {
    public static Pageable createPageable(Integer pageNumber, Integer pageSize) {
        int safePage = pageNumber == null ? 0 : Math.max(pageNumber, 0);
        int safeSize = pageSize == null ? 5 :Math.min(Math.max(pageSize, 1), 5);
        return PageRequest.of(safePage, safeSize);
    }

    public static ReviewResponse toReviewDto(Review review) {
        return toReviewDto(review, null);
    }

    public static ReviewResponse toReviewDto(Review review, UUID currentUserId) {
        return ReviewResponse.builder()
                .id(review.getId())
                .username(review.getUsername())
                .reviewDescription(review.getReviewDescription())
                .createdAt(review.getCreatedAt())
                .rating(review.getRating())
                .photos(review.getPhotos() != null ? new ArrayList<>(review.getPhotos()) : new ArrayList<>())
                .upvoteCount(review.getUpvotedUserIds() != null ? review.getUpvotedUserIds().size() : 0)
                .upvotedByCurrentUser(review.getUpvotedUserIds() != null && currentUserId != null && review.getUpvotedUserIds().contains(currentUserId))
                .build();
    }
}
