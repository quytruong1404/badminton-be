package com.quy.badmintonbe.booking.repository;

import com.quy.badmintonbe.booking.entity.BookingServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingServiceItemRepository extends JpaRepository<BookingServiceItem, Long> {
    List<BookingServiceItem> findByBookingId(Long bookingId);

    @Query(value = "SELECT COALESCE(SUM(bsi.quantity), 0) FROM booking_services bsi " +
                   "JOIN booking_details bd ON bd.booking_id = bsi.booking_id " +
                   "JOIN bookings b ON b.id = bsi.booking_id " +
                   "WHERE bsi.product_id = :productId " +
                   "AND bd.booking_date = :bookingDate " +
                   "AND bd.slot_id = :slotId " +
                   "AND b.booking_status IN (:statuses)", nativeQuery = true)
    Integer countRentedQuantityInSlot(@Param("productId") Long productId,
                                     @Param("bookingDate") LocalDate bookingDate,
                                     @Param("slotId") Long slotId,
                                     @Param("statuses") List<String> statuses);
}
