package com.quy.badmintonbe.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionScheduleDto {
    private Long id;
    private Long courtId;
    private String courtName;
    private Long slotId;
    private String startTime;
    private String endTime;
    private Integer dayOfWeek;
    private String status;
}
