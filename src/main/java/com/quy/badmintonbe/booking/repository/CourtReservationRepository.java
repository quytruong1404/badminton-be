package com.quy.badmintonbe.booking.repository;

import com.quy.badmintonbe.booking.entity.CourtReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quy.badmintonbe.common.enums.ReservationSourceType;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourtReservationRepository extends JpaRepository<CourtReservation, Long> {
    List<CourtReservation> findByCourtIdAndReservationDate(Long courtId, LocalDate reservationDate);
    List<CourtReservation> findByReservationDate(LocalDate reservationDate);
    List<CourtReservation> findBySourceTypeAndSourceId(ReservationSourceType sourceType, Long sourceId);
    boolean existsBySlotId(Long slotId);
}
