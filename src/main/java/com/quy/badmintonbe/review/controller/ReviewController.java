package com.quy.badmintonbe.review.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.review.dto.ReviewDto;
import com.quy.badmintonbe.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(@PathVariable Long id) {
        ReviewDto review = reviewService.getReviewById(id);
        ApiResponse<ReviewDto> response = ApiResponse.<ReviewDto>builder()
                .success(true)
                .message("Review retrieved successfully")
                .data(review)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviews(
            @RequestParam(required = false) Long courtId,
            @RequestParam(required = false) Long userId) {
        List<ReviewDto> reviews;
        if (courtId != null) {
            reviews = reviewService.getReviewsByCourtId(courtId);
        } else if (userId != null) {
            reviews = reviewService.getReviewsByUserId(userId);
        } else {
            reviews = reviewService.getAllReviews();
        }
        ApiResponse<List<ReviewDto>> response = ApiResponse.<List<ReviewDto>>builder()
                .success(true)
                .message("Reviews retrieved successfully")
                .data(reviews)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(@RequestBody ReviewDto reviewDto) {
        ReviewDto createdReview = reviewService.createReview(reviewDto);
        ApiResponse<ReviewDto> response = ApiResponse.<ReviewDto>builder()
                .success(true)
                .message("Review created successfully")
                .data(createdReview)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Review deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
