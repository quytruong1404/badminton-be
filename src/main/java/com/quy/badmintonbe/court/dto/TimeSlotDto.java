package com.quy.badmintonbe.court.dto;

import com.quy.badmintonbe.common.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDto {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
