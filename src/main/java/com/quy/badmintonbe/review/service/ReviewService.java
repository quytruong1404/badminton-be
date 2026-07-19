package com.quy.badmintonbe.review.service;

import com.quy.badmintonbe.review.dto.ReviewDto;
import java.util.List;

public interface ReviewService {
    ReviewDto getReviewById(Long id);
    List<ReviewDto> getReviewsByCourtId(Long courtId);
    List<ReviewDto> getReviewsByUserId(Long userId);
    List<ReviewDto> getAllReviews();
    ReviewDto createReview(ReviewDto reviewDto);
    void deleteReview(Long id);
}
