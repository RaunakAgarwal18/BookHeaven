package com.bookheaven.review_service.service.impl;

import com.bookheaven.review_service.repository.ReviewRepository;
import com.bookheaven.review_service.dto.requestDto.AddReviewRequest;
import com.bookheaven.review_service.dto.ResponseDto.ReviewResponse;
import com.bookheaven.review_service.dto.requestDto.UpdateReviewRequest;
import com.bookheaven.common.dto.event.BookRatingUpdateEvent;
import com.bookheaven.review_service.entity.Review;
import com.bookheaven.review_service.exception.DuplicateReviewException;
import com.bookheaven.review_service.exception.ReviewNotFoundException;
import com.bookheaven.review_service.exception.UnauthorizedReviewAccessException;
import com.bookheaven.review_service.service.ReviewService;
import com.bookheaven.review_service.util.AppUtil;
import com.bookheaven.review_service.util.JwtUtil;
import com.bookheaven.review_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public ReviewResponse addReview(Authentication authentication, AddReviewRequest reviewRequest) {
        UUID userId = jwtUtil.extractId((String) authentication.getCredentials());
        if (reviewRepository.existsByUserIdAndBookId(userId, reviewRequest.getBookId())) {
            throw new DuplicateReviewException("You have already reviewed this book");
        }
        Review review = Review.builder()
                .reviewDescription(reviewRequest.getReviewDescription())
                .rating(reviewRequest.getRating())
                .bookId(reviewRequest.getBookId())
                .userId(userId)
                .username(jwtUtil.extractUsername((String)authentication.getCredentials()))
                .photos(reviewRequest.getPhotos() != null ? reviewRequest.getPhotos() : new ArrayList<>())
                .build();
        Review savedReview = reviewRepository.save(review);
        updateBookRating(reviewRequest.getBookId());
        return AppUtil.toReviewDto(savedReview, userId);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Authentication authentication, Long id, UpdateReviewRequest reviewRequest) throws RuntimeException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        UUID requesterId = jwtUtil.extractId((String) authentication.getCredentials());
        if (!review.getUserId().equals(requesterId)) {
            throw new UnauthorizedReviewAccessException("You cannot update a review written by someone else");
        }
        review.setReviewDescription(reviewRequest.getReviewDescription());
        review.setRating(reviewRequest.getRating());
        if (reviewRequest.getPhotos() != null) {
            review.setPhotos(reviewRequest.getPhotos());
        }
        Review savedReview = reviewRepository.save(review);
        updateBookRating(review.getBookId());
        return AppUtil.toReviewDto(savedReview, requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> findReviewByBookId(Authentication authentication, Long id, Integer pageNumber, Integer pageSize) {
        Pageable pageable = AppUtil.createPageable(pageNumber, pageSize);
        UUID currentUserId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                currentUserId = jwtUtil.extractId((String) authentication.getCredentials());
            } catch (Exception e) {
                // Ignore if token is invalid or not a user authentication
            }
        }
        final UUID finalCurrentUserId = currentUserId;
        return reviewRepository.findByBookId(id, pageable)
                .map(review -> AppUtil.toReviewDto(review, finalCurrentUserId));
    }

    @Override
    @Transactional
    public void deleteReview(Authentication authentication, Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        UUID requesterId = jwtUtil.extractId((String) authentication.getCredentials());
        if (!review.getUserId().equals(requesterId)) {
            throw new UnauthorizedReviewAccessException("You cannot delete a review written by someone else");
        }
        Long bookId = review.getBookId();
        reviewRepository.delete(review);
        updateBookRating(bookId);
    }

    @Override
    @Transactional
    @org.springframework.retry.annotation.Retryable(
            retryFor = org.springframework.orm.ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 100)
    )
    public ReviewResponse toggleUpvote(Authentication authentication, Long id) {
        UUID userId = jwtUtil.extractId((String) authentication.getCredentials());
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + id));
        
        if (review.getUpvotedUserIds().contains(userId)) {
            review.getUpvotedUserIds().remove(userId);
            log.info("User {} removed upvote from review {}", userId, id);
        } else {
            review.getUpvotedUserIds().add(userId);
            log.info("User {} upvoted review {}", userId, id);
        }
        Review savedReview = reviewRepository.save(review);
        return AppUtil.toReviewDto(savedReview, userId);
    }

    private void updateBookRating(Long bookId) {
        try {
            Double averageRating = reviewRepository.getAverageRatingByBookId(bookId);
            Integer totalReviews = reviewRepository.countByBookId(bookId);
            
            if (averageRating == null) averageRating = 0.0;
            if (totalReviews == null) totalReviews = 0;

            BookRatingUpdateEvent event = BookRatingUpdateEvent.builder()
                    .bookId(bookId)
                    .averageRating(averageRating)
                    .totalReviews(totalReviews)
                    .build();

            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(
                            RabbitMQConfig.RATING_UPDATE_EXCHANGE, 
                            RabbitMQConfig.RATING_UPDATE_ROUTING_KEY, 
                            event
                        );
                        log.info("Successfully published rating update event for bookId: {}", bookId);
                    }
                }
            );
        } catch (Exception e) {
            log.error("Failed to publish rating update event for bookId: {}", bookId, e);
        }
    }
}
