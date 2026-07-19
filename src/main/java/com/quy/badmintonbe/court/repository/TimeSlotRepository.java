package com.quy.badmintonbe.court.repository;

import com.quy.badmintonbe.court.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);
}
