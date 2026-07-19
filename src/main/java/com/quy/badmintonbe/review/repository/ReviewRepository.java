package com.quy.badmintonbe.review.repository;

import com.quy.badmintonbe.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourtId(Long courtId);
    List<Review> findByUserId(Long userId);
    boolean existsByBookingDetailId(Long bookingDetailId);
    java.util.Optional<Review> findByBookingDetailId(Long bookingDetailId);
}
