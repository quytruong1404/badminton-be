package com.quy.badmintonbe.court.service;

import com.quy.badmintonbe.court.dto.TimeSlotDto;
import java.util.List;

public interface TimeSlotService {
    TimeSlotDto getTimeSlotById(Long id);
    List<TimeSlotDto> getAllTimeSlots();
    TimeSlotDto createTimeSlot(TimeSlotDto timeSlotDto);
    TimeSlotDto updateTimeSlot(Long id, TimeSlotDto timeSlotDto);
    void deleteTimeSlot(Long id);
}
