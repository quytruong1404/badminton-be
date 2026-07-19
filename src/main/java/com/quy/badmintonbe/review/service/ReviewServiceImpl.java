package com.quy.badmintonbe.review.service;

import com.quy.badmintonbe.booking.entity.BookingDetail;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.review.dto.ReviewDto;
import com.quy.badmintonbe.review.entity.Review;
import com.quy.badmintonbe.review.repository.ReviewRepository;
import com.quy.badmintonbe.user.entity.User;
import com.quy.badmintonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourtRepository courtRepository;
    private final BookingDetailRepository bookingDetailRepository;

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        return mapToDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByCourtId(Long courtId) {
        return reviewRepository.findByCourtId(courtId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto createReview(ReviewDto dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new com.quy.badmintonbe.common.exception.BadRequestException("Điểm đánh giá phải từ 1 đến 5 sao.");
        }
        
        if (reviewRepository.existsByBookingDetailId(dto.getBookingDetailId())) {
            throw new com.quy.badmintonbe.common.exception.BadRequestException("Lịch chơi này đã được đánh giá trước đó.");
        }
        
        BookingDetail detail = bookingDetailRepository.findById(dto.getBookingDetailId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đặt ca với ID: " + dto.getBookingDetailId()));
        
        if (!detail.getBooking().getUser().getId().equals(dto.getUserId())) {
            throw new com.quy.badmintonbe.common.exception.BadRequestException("Bạn không có quyền đánh giá lịch chơi này.");
        }
        
        java.time.LocalDate today = java.time.LocalDate.now();
        if (detail.getBookingDate().isAfter(today)) {
            throw new com.quy.badmintonbe.common.exception.BadRequestException("Bạn chỉ có thể đánh giá sau khi ca thi đấu đã diễn ra.");
        }

        Review review = Review.builder()
                .user(detail.getBooking().getUser())
                .court(detail.getCourt())
                .bookingDetail(detail)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + id));
        reviewRepository.delete(review);
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .courtId(review.getCourt().getId())
                .bookingDetailId(review.getBookingDetail().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private Review mapToEntity(ReviewDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));
        Court court = courtRepository.findById(dto.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + dto.getCourtId()));
        BookingDetail detail = bookingDetailRepository.findById(dto.getBookingDetailId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đặt ca với ID: " + dto.getBookingDetailId()));

        return Review.builder()
                .id(dto.getId())
                .user(user)
                .court(court)
                .bookingDetail(detail)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
    }
}
