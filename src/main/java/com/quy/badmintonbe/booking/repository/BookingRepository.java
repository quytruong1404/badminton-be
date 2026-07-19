package com.quy.badmintonbe.booking.repository;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByBookingStatusAndPaymentStatusAndCreatedAtBefore(
            BookingStatus bookingStatus,
            PaymentStatus paymentStatus,
            LocalDateTime createdAtBefore
    );
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE b.bookingStatus = com.quy.badmintonbe.common.enums.BookingStatus.CONFIRMED AND " +
           "NOT EXISTS (SELECT bd FROM BookingDetail bd WHERE bd.booking.id = b.id AND bd.bookingDate >= :today)")
    List<Booking> findCompletedBookings(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
}
