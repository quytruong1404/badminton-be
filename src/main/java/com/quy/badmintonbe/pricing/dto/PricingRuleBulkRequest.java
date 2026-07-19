package com.quy.badmintonbe.pricing.dto;

import com.quy.badmintonbe.common.enums.DayType;
import com.quy.badmintonbe.common.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleBulkRequest {
    private List<Long> courtIds;
    private List<Long> slotIds;
    private List<DayType> dayTypes;
    private BigDecimal price;
    private SlotStatus status;
}
