package com.quy.badmintonbe.court.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.court.dto.TimeSlotDto;
import com.quy.badmintonbe.court.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getAllTimeSlots() {
        List<TimeSlotDto> slots = timeSlotService.getAllTimeSlots();
        ApiResponse<List<TimeSlotDto>> response = ApiResponse.<List<TimeSlotDto>>builder()
                .success(true)
                .message("All time slots retrieved successfully")
                .data(slots)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeSlotDto>> createTimeSlot(@RequestBody TimeSlotDto slotDto) {
        TimeSlotDto saved = timeSlotService.createTimeSlot(slotDto);
        ApiResponse<TimeSlotDto> response = ApiResponse.<TimeSlotDto>builder()
                .success(true)
                .message("Time slot created successfully")
                .data(saved)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeSlotDto>> updateTimeSlot(
            @PathVariable Long id, @RequestBody TimeSlotDto slotDto) {
        TimeSlotDto updated = timeSlotService.updateTimeSlot(id, slotDto);
        ApiResponse<TimeSlotDto> response = ApiResponse.<TimeSlotDto>builder()
                .success(true)
                .message("Time slot updated successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Time slot deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
