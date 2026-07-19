package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.dto.BookingCreateRequest;
import com.quy.badmintonbe.booking.dto.BookingResponse;
import java.util.List;

public interface BookingService {
    BookingResponse getBookingById(Long id);
    BookingResponse getBookingByCode(String bookingCode);
    List<BookingResponse> getBookingsByUserId(Long userId);
    List<BookingResponse> getAllBookings();
    BookingResponse createBooking(BookingCreateRequest bookingCreateDto);
    BookingResponse updateBooking(Long id, BookingResponse bookingDto);
    void cancelBooking(Long id, String reason);
    List<Long> getOccupiedSlots(Long courtId, String date);
}
