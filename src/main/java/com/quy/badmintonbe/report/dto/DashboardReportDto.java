package com.quy.badmintonbe.report.dto;

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
public class DashboardReportDto {
    private double totalRevenue;
    private long totalBookingsCount;
    private long totalSubscriptionsCount;
    private double cancellationRate;

    private List<RevenueDataPoint> revenueDaily;
    private List<RevenueDataPoint> revenueMonthly;
    private List<RevenueDataPoint> revenueYearly;

    private List<CourtRevenueDto> topCourtsByRevenue;
    private List<SlotBookingCountDto> topSlotsByBooking;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueDataPoint {
        private String label;
        private double amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourtRevenueDto {
        private Long courtId;
        private String courtName;
        private String branchName;
        private double revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotBookingCountDto {
        private Long slotId;
        private String timeRange;
        private long bookingCount;
    }
}
