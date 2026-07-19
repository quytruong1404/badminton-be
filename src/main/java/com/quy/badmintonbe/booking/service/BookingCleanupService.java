package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import com.quy.badmintonbe.systemconfig.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final SystemConfigRepository systemConfigRepository;

    /**
     * Chạy mỗi 1 phút để kiểm tra các đơn đặt sân ở trạng thái PENDING nhưng
     * vẫn chưa thanh toán (UNPAID) lâu hơn cấu hình hệ thống TIMEOUT_MINS (mặc định: 15 phút).
     * Các đơn đặt này sẽ bị hủy tự động và giải phóng lịch giữ sân liên quan.
     */
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void cleanupExpiredBookings() {
        int timeoutMins = 15;
        try {
            timeoutMins = systemConfigRepository.findByConfigKey("TIMEOUT_MINS")
                    .map(config -> Integer.parseInt(config.getConfigValue()))
                    .orElse(15);
        } catch (Exception e) {
            log.warn("Không thể đọc cấu hình TIMEOUT_MINS từ cơ sở dữ liệu. Sử dụng giá trị mặc định là 15 phút.", e);
        }

        LocalDateTime expireThreshold = LocalDateTime.now().minusMinutes(timeoutMins);

        List<Booking> expiredBookings = bookingRepository
                .findByBookingStatusAndPaymentStatusAndCreatedAtBefore(
                        BookingStatus.PENDING,
                        PaymentStatus.UNPAID,
                        expireThreshold
                );

        if (!expiredBookings.isEmpty()) {
            log.info("Tìm thấy {} đơn đặt sân cầu lông quá hạn thanh toán. Bắt đầu tự động dọn dẹp và hủy đơn...", expiredBookings.size());
            for (Booking booking : expiredBookings) {
                try {
                    bookingService.cancelBooking(booking.getId(), "Hệ thống tự động hủy đơn do quá hạn thanh toán");
                    log.info("Đã tự động hủy thành công đơn đặt sân quá hạn: {}", booking.getBookingCode());
                } catch (Exception e) {
                    log.error("Hủy tự động đơn đặt sân quá hạn thất bại: " + booking.getBookingCode(), e);
                }
            }
        }
    }

    /**
     * Chạy vào lúc 1:00 AM mỗi ngày để quét các đơn hàng có trạng thái CONFIRMED
     * nhưng tất cả các ngày đặt chơi đã qua (nhỏ hơn ngày hiện tại).
     * Các đơn hàng này sẽ được tự động cập nhật trạng thái thành COMPLETED (Hoàn tất).
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoUpdateCompletedBookings() {
        log.info("Bắt đầu quét và tự động hoàn tất các đơn đặt sân đã chơi xong...");
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Booking> completedBookings = bookingRepository.findCompletedBookings(today);

        if (!completedBookings.isEmpty()) {
            log.info("Tìm thấy {} đơn đặt sân đã chơi xong cần cập nhật thành COMPLETED.", completedBookings.size());
            for (Booking booking : completedBookings) {
                try {
                    booking.setBookingStatus(BookingStatus.COMPLETED);
                    bookingRepository.save(booking);
                    log.info("Đã tự động hoàn tất thành công đơn đặt sân: {}", booking.getBookingCode());
                } catch (Exception e) {
                    log.error("Tự động hoàn tất đơn đặt sân thất bại: " + booking.getBookingCode(), e);
                }
            }
        }
    }
}
