package com.bookheaven.review_service.service;

import com.bookheaven.review_service.dto.requestDto.AddReviewRequest;
import com.bookheaven.review_service.dto.ResponseDto.ReviewResponse;
import com.bookheaven.review_service.dto.requestDto.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;


public interface ReviewService {
    public ReviewResponse addReview(Authentication authentication, AddReviewRequest reviewRequest);
    public ReviewResponse updateReview(Authentication authentication, Long id, UpdateReviewRequest reviewRequest);
    public Page<ReviewResponse> findReviewByBookId(Authentication authentication, Long id, Integer pageNumber, Integer pageSize);
    public void deleteReview(Authentication authentication, Long id);
    public ReviewResponse toggleUpvote(Authentication authentication, Long id);
}
