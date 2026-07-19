package com.quy.badmintonbe.booking.repository;

import com.quy.badmintonbe.booking.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    List<BookingDetail> findByBookingId(Long bookingId);
    boolean existsBySlotId(Long slotId);
    boolean existsByCourtId(Long courtId);
}
