package com.quy.badmintonbe.booking.dto;

import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long voucherId;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookingDetailResponse> details;
    private List<BookingServiceResponse> services;
}
