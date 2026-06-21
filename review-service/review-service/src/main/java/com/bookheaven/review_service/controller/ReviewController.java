package com.bookheaven.review_service.controller;

import com.bookheaven.review_service.dto.requestDto.AddReviewRequest;
import com.bookheaven.review_service.dto.ResponseDto.ReviewResponse;
import com.bookheaven.review_service.dto.requestDto.UpdateReviewRequest;
import com.bookheaven.review_service.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{bookId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByBookId(Authentication authentication, @PathVariable Long bookId, @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize){
        Page<ReviewResponse> reviews = reviewService.findReviewByBookId(authentication, bookId,pageNumber,pageSize);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("")
    public ResponseEntity<ReviewResponse> addReview(Authentication authentication, @RequestBody AddReviewRequest reviewRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(authentication, reviewRequest));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(Authentication authentication, @PathVariable Long reviewId, @RequestBody UpdateReviewRequest reviewRequest){
        return ResponseEntity.ok(reviewService.updateReview(authentication, reviewId, reviewRequest));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(Authentication authentication, @PathVariable Long reviewId){
        reviewService.deleteReview(authentication, reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/upvote")
    public ResponseEntity<ReviewResponse> toggleUpvote(Authentication authentication, @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.toggleUpvote(authentication, reviewId));
    }
}
