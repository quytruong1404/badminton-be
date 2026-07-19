package com.quy.badmintonbe.booking.controller;

import com.quy.badmintonbe.booking.dto.BookingCreateRequest;
import com.quy.badmintonbe.booking.dto.BookingResponse;
import com.quy.badmintonbe.booking.service.BookingService;
import com.quy.badmintonbe.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking retrieved successfully")
                .data(booking)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByCode(@PathVariable String bookingCode) {
        BookingResponse booking = bookingService.getBookingByCode(bookingCode);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking retrieved successfully")
                .data(booking)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookings(
            @RequestParam(required = false) Long userId) {
        List<BookingResponse> bookings;
        if (userId != null) {
            bookings = bookingService.getBookingsByUserId(userId);
        } else {
            bookings = bookingService.getAllBookings();
        }
        ApiResponse<List<BookingResponse>> response = ApiResponse.<List<BookingResponse>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookings)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@RequestBody BookingCreateRequest bookingDto) {
        BookingResponse createdBooking = bookingService.createBooking(bookingDto);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking created successfully")
                .data(createdBooking)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id, @RequestBody BookingResponse bookingDto) {
        BookingResponse updatedBooking = bookingService.updateBooking(id, bookingDto);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking updated successfully")
                .data(updatedBooking)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        bookingService.cancelBooking(id, reason);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Booking cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    // Get list of occupied slot IDs for a specific court and date
    @GetMapping("/occupied-slots")
    public ResponseEntity<ApiResponse<List<Long>>> getOccupiedSlots(
            @RequestParam Long courtId,
            @RequestParam String date) {
        List<Long> occupiedSlots = bookingService.getOccupiedSlots(courtId, date);

        ApiResponse<List<Long>> response = ApiResponse.<List<Long>>builder()
                .success(true)
                .message("Occupied slots retrieved successfully")
                .data(occupiedSlots)
                .build();
        return ResponseEntity.ok(response);
    }
}
