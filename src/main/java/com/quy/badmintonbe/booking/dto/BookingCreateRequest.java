package com.quy.badmintonbe.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequest {
    private Long userId;
    private Long voucherId;
    private List<BookingDetailRequest> details;
    private List<BookingServiceRequest> services;
}
