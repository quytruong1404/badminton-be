package com.quy.badmintonbe.subscription.dto;

import com.quy.badmintonbe.common.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private Long id;
    private String subscriptionCode;
    private Long userId;
    private Long branchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private SubscriptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SubscriptionScheduleDto> schedules;
    private Long voucherId;
    private BigDecimal discountAmount;
}
