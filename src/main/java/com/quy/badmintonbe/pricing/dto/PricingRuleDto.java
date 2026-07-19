package com.quy.badmintonbe.pricing.dto;

import com.quy.badmintonbe.common.enums.DayType;
import com.quy.badmintonbe.common.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleDto {
    private Long id;
    private Long courtId;
    private Long slotId;
    private DayType dayType;
    private BigDecimal price;
    private SlotStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
