package com.quy.badmintonbe.report.service;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.entity.BookingDetail;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.SubscriptionStatus;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.entity.TimeSlot;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.report.dto.DashboardReportDto;
import com.quy.badmintonbe.subscription.entity.Subscription;
import com.quy.badmintonbe.subscription.entity.SubscriptionSchedule;
import com.quy.badmintonbe.subscription.repository.SubscriptionRepository;
import com.quy.badmintonbe.subscription.repository.SubscriptionScheduleRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SubscriptionScheduleRepository subscriptionScheduleRepository;

    @Override
    public DashboardReportDto getDashboardReport() {
        List<Booking> bookings = bookingRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        List<BookingDetail> bookingDetails = bookingDetailRepository.findAll();
        List<Court> courts = courtRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();

        // 1. Tính toán số liệu thống kê chung
        double totalRevenue = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED || b.getBookingStatus() == BookingStatus.COMPLETED)
                .mapToDouble(b -> b.getTotalPrice().doubleValue())
                .sum() +
                subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.EXPIRED)
                .mapToDouble(s -> s.getTotalPrice().doubleValue())
                .sum();

        long totalBookingsCount = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED || b.getBookingStatus() == BookingStatus.COMPLETED)
                .count();

        long totalSubscriptionsCount = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.EXPIRED)
                .count();

        // Tỷ lệ hủy sân
        long totalDetailsCount = bookingDetails.size();
        long cancelledDetailsCount = bookingDetails.stream()
                .filter(d -> "CANCELLED".equals(d.getDetailStatus()))
                .count();
        double cancellationRate = totalDetailsCount > 0 ? ((double) cancelledDetailsCount / totalDetailsCount) * 100 : 0.0;

        // 2. Doanh thu theo ngày, tháng, năm
        Map<String, Double> dailyMap = new HashMap<>();
        Map<String, Double> monthlyMap = new HashMap<>();
        Map<String, Double> yearlyMap = new HashMap<>();

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

        // Cộng doanh thu từ đơn đặt ca lẻ
        bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED || b.getBookingStatus() == BookingStatus.COMPLETED)
                .forEach(b -> {
                    String day = b.getCreatedAt().format(dayFormatter);
                    String month = b.getCreatedAt().format(monthFormatter);
                    String year = b.getCreatedAt().format(yearFormatter);
                    double price = b.getTotalPrice().doubleValue();

                    dailyMap.put(day, dailyMap.getOrDefault(day, 0.0) + price);
                    monthlyMap.put(month, monthlyMap.getOrDefault(month, 0.0) + price);
                    yearlyMap.put(year, yearlyMap.getOrDefault(year, 0.0) + price);
                });

        // Cộng doanh thu từ lịch đặt cố định
        subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.EXPIRED)
                .forEach(s -> {
                    String day = s.getCreatedAt().format(dayFormatter);
                    String month = s.getCreatedAt().format(monthFormatter);
                    String year = s.getCreatedAt().format(yearFormatter);
                    double price = s.getTotalPrice().doubleValue();

                    dailyMap.put(day, dailyMap.getOrDefault(day, 0.0) + price);
                    monthlyMap.put(month, monthlyMap.getOrDefault(month, 0.0) + price);
                    yearlyMap.put(year, yearlyMap.getOrDefault(year, 0.0) + price);
                });

        // Chuyển sang danh sách đã sắp xếp
        List<DashboardReportDto.RevenueDataPoint> revenueDaily = dailyMap.entrySet().stream()
                .map(e -> DashboardReportDto.RevenueDataPoint.builder().label(e.getKey()).amount(e.getValue()).build())
                .sorted(Comparator.comparing(DashboardReportDto.RevenueDataPoint::getLabel))
                .collect(Collectors.toList());

        // Giới hạn 30 ngày gần nhất cho biểu đồ ngày để tránh quá tải hiển thị
        if (revenueDaily.size() > 30) {
            revenueDaily = revenueDaily.subList(revenueDaily.size() - 30, revenueDaily.size());
        }

        List<DashboardReportDto.RevenueDataPoint> revenueMonthly = monthlyMap.entrySet().stream()
                .map(e -> DashboardReportDto.RevenueDataPoint.builder().label(e.getKey()).amount(e.getValue()).build())
                .sorted(Comparator.comparing(DashboardReportDto.RevenueDataPoint::getLabel))
                .collect(Collectors.toList());

        List<DashboardReportDto.RevenueDataPoint> revenueYearly = yearlyMap.entrySet().stream()
                .map(e -> DashboardReportDto.RevenueDataPoint.builder().label(e.getKey()).amount(e.getValue()).build())
                .sorted(Comparator.comparing(DashboardReportDto.RevenueDataPoint::getLabel))
                .collect(Collectors.toList());

        // 3. Doanh thu theo sân đấu
        Map<Long, Double> courtRevenueMap = new HashMap<>();
        bookingDetails.stream()
                .filter(d -> !"CANCELLED".equals(d.getDetailStatus()) && 
                             (d.getBooking().getBookingStatus() == BookingStatus.CONFIRMED || d.getBooking().getBookingStatus() == BookingStatus.COMPLETED))
                .forEach(d -> {
                    Long courtId = d.getCourt().getId();
                    double price = d.getUnitPrice().doubleValue();
                    courtRevenueMap.put(courtId, courtRevenueMap.getOrDefault(courtId, 0.0) + price);
                });

        // Cộng thêm doanh thu từ lịch đặt cố định (SubscriptionSchedules)
        List<SubscriptionSchedule> subscriptionSchedules = subscriptionScheduleRepository.findAll();
        
        // Nhóm lịch theo từng hợp đồng cố định để tính tổng số ca và phân bổ tỷ lệ doanh thu
        Map<Long, List<SubscriptionSchedule>> schedulesBySub = subscriptionSchedules.stream()
                .filter(sched -> sched.getSubscription().getStatus() == SubscriptionStatus.ACTIVE || sched.getSubscription().getStatus() == SubscriptionStatus.EXPIRED)
                .collect(Collectors.groupingBy(sched -> sched.getSubscription().getId()));

        schedulesBySub.forEach((subId, list) -> {
            if (list.isEmpty()) return;
            Subscription sub = list.get(0).getSubscription();
            
            // Tính số buổi cho mỗi lịch lặp lại
            Map<SubscriptionSchedule, Integer> sessionCounts = new HashMap<>();
            int totalSessionsInSub = 0;
            for (SubscriptionSchedule sched : list) {
                int count = calculateSessionsCount(
                        sub.getStartDate(),
                        sub.getEndDate(),
                        sched.getDayOfWeek()
                );
                sessionCounts.put(sched, count);
                totalSessionsInSub += count;
            }

            if (totalSessionsInSub > 0) {
                double subTotalPrice = sub.getTotalPrice().doubleValue();
                double avgPricePerSession = subTotalPrice / totalSessionsInSub;
                
                for (SubscriptionSchedule sched : list) {
                    Long courtId = sched.getCourt().getId();
                    int count = sessionCounts.get(sched);
                    double allocatedRevenue = count * avgPricePerSession;
                    courtRevenueMap.put(courtId, courtRevenueMap.getOrDefault(courtId, 0.0) + allocatedRevenue);
                }
            }
        });

        List<DashboardReportDto.CourtRevenueDto> topCourtsByRevenue = courts.stream()
                .map(c -> {
                    double revenue = courtRevenueMap.getOrDefault(c.getId(), 0.0);
                    return DashboardReportDto.CourtRevenueDto.builder()
                            .courtId(c.getId())
                            .courtName(c.getName())
                            .branchName(c.getBranch().getName())
                            .revenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardReportDto.CourtRevenueDto::getRevenue).reversed())
                .collect(Collectors.toList());

        // 4. Khung giờ được đặt nhiều nhất
        Map<Long, Long> slotCountMap = bookingDetails.stream()
                .filter(d -> !"CANCELLED".equals(d.getDetailStatus()) &&
                             (d.getBooking().getBookingStatus() == BookingStatus.CONFIRMED || d.getBooking().getBookingStatus() == BookingStatus.COMPLETED))
                .collect(Collectors.groupingBy(d -> d.getSlot().getId(), Collectors.counting()));

        // Cộng thêm lượt đặt từ lịch đặt cố định (SubscriptionSchedules)
        subscriptionSchedules.stream()
                .filter(sched -> sched.getSubscription().getStatus() == SubscriptionStatus.ACTIVE || sched.getSubscription().getStatus() == SubscriptionStatus.EXPIRED)
                .forEach(sched -> {
                    Long slotId = sched.getSlot().getId();
                    int sessionsCount = calculateSessionsCount(
                            sched.getSubscription().getStartDate(),
                            sched.getSubscription().getEndDate(),
                            sched.getDayOfWeek()
                    );
                    slotCountMap.put(slotId, slotCountMap.getOrDefault(slotId, 0L) + sessionsCount);
                });

        List<DashboardReportDto.SlotBookingCountDto> topSlotsByBooking = slots.stream()
                .map(s -> {
                    long count = slotCountMap.getOrDefault(s.getId(), 0L);
                    String timeRange = s.getStartTime() + " - " + s.getEndTime();
                    return DashboardReportDto.SlotBookingCountDto.builder()
                            .slotId(s.getId())
                            .timeRange(timeRange)
                            .bookingCount(count)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardReportDto.SlotBookingCountDto::getBookingCount).reversed())
                .collect(Collectors.toList());

        return DashboardReportDto.builder()
                .totalRevenue(totalRevenue)
                .totalBookingsCount(totalBookingsCount)
                .totalSubscriptionsCount(totalSubscriptionsCount)
                .cancellationRate(cancellationRate)
                .revenueDaily(revenueDaily)
                .revenueMonthly(revenueMonthly)
                .revenueYearly(revenueYearly)
                .topCourtsByRevenue(topCourtsByRevenue)
                .topSlotsByBooking(topSlotsByBooking)
                .build();
    }

    private int calculateSessionsCount(LocalDate start, LocalDate end, int dayOfWeek) {
        if (start == null || end == null || start.isAfter(end)) return 0;
        int count = 0;
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            int currentDay = curr.getDayOfWeek().getValue();
            if (currentDay == dayOfWeek) {
                count++;
            }
            curr = curr.plusDays(1);
        }
        return count;
    }
}
