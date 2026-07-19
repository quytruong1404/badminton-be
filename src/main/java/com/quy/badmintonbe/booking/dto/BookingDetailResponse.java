package com.quy.badmintonbe.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetailResponse {
    private Long id;
    private Long courtId;
    private String courtName;
    private String branchName;
    private Long slotId;
    private String startTime;
    private String endTime;
    private LocalDate bookingDate;
    private BigDecimal unitPrice;
    private String detailStatus;
}
