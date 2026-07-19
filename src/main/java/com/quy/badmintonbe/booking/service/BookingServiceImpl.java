package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.dto.BookingCreateRequest;
import com.quy.badmintonbe.booking.dto.BookingDetailRequest;
import com.quy.badmintonbe.booking.dto.BookingResponse;
import com.quy.badmintonbe.booking.dto.BookingServiceRequest;
import com.quy.badmintonbe.booking.dto.BookingDetailResponse;
import com.quy.badmintonbe.booking.dto.BookingServiceResponse;
import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.entity.BookingDetail;
import com.quy.badmintonbe.booking.entity.BookingServiceItem;
import com.quy.badmintonbe.booking.entity.CancellationPolicy;
import com.quy.badmintonbe.booking.entity.CourtReservation;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.booking.repository.BookingServiceRepository;
import com.quy.badmintonbe.booking.repository.CourtReservationRepository;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.DayType;
import com.quy.badmintonbe.common.enums.DiscountType;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import com.quy.badmintonbe.common.enums.ReservationSourceType;
import com.quy.badmintonbe.common.enums.ReservationStatus;
import com.quy.badmintonbe.common.exception.BadRequestException;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.entity.TimeSlot;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.pricing.entity.PricingRule;
import com.quy.badmintonbe.pricing.repository.PricingRuleRepository;
import com.quy.badmintonbe.product.entity.Product;
import com.quy.badmintonbe.product.repository.ProductRepository;
import com.quy.badmintonbe.user.entity.User;
import com.quy.badmintonbe.user.repository.UserRepository;
import com.quy.badmintonbe.voucher.entity.Voucher;
import com.quy.badmintonbe.voucher.repository.VoucherRepository;
import com.quy.badmintonbe.payment.repository.PaymentRepository;
import com.quy.badmintonbe.payment.repository.RefundRepository;
import com.quy.badmintonbe.payment.entity.Payment;
import com.quy.badmintonbe.payment.entity.Refund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final CourtReservationRepository courtReservationRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final BookingServiceRepository bookingServiceRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + id));
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse getBookingByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với mã: " + bookingCode));
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest dto) {
        // 1. Verify user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));

        // Generate a unique booking code: BK-YYMMDD-XXXX (e.g. BK-260702-0001)
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(vietnamZone);
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        long countToday = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        String bookingCode = String.format("BK-%s-%04d", dateStr, countToday + 1);

        // Khởi tạo các biến tính tổng tiền
        BigDecimal subTotal = BigDecimal.ZERO;

        // 2. Kiểm tra tính hợp lệ và giữ lịch các ca đấu
        List<BookingDetail> bookingDetailsToSave = new ArrayList<>();
        List<CourtReservation> reservationsToSave = new ArrayList<>();

        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new BadRequestException("Phải cung cấp ít nhất một thông tin chi tiết đặt sân.");
        }

        for (BookingDetailRequest detailDto : dto.getDetails()) {
            Court court = courtRepository.findById(detailDto.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + detailDto.getCourtId()));

            TimeSlot slot = timeSlotRepository.findById(detailDto.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + detailDto.getSlotId()));

            com.quy.badmintonbe.branch.entity.Branch branch = court.getBranch();
            if (branch != null) {
                java.time.LocalTime startTime = slot.getStartTime();
                java.time.LocalTime endTime = slot.getEndTime();
                java.time.LocalTime openTime = branch.getOpenTime();
                java.time.LocalTime closeTime = branch.getCloseTime();

                if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                    throw new BadRequestException("Ca chơi " + startTime.toString().substring(0, 5) 
                            + " - " + endTime.toString().substring(0, 5) 
                            + " nằm ngoài giờ hoạt động của chi nhánh " + branch.getName() 
                            + " (" + openTime.toString().substring(0, 5) + " - " + closeTime.toString().substring(0, 5) + ").");
                }
            }

            LocalDate bookingDate = detailDto.getBookingDate();
            if (bookingDate == null || bookingDate.isBefore(today)) {
                throw new BadRequestException("Ngày đặt sân phải là hôm nay hoặc các ngày tiếp theo trong tương lai.");
            }

            if (bookingDate.equals(today)) {
                LocalTime now = LocalTime.now(vietnamZone);
                if (slot.getStartTime().isBefore(now)) {
                    throw new BadRequestException("Khung giờ ca đấu " + slot.getStartTime().toString().substring(0, 5) 
                            + " - " + slot.getEndTime().toString().substring(0, 5) + " ngày hôm nay đã trôi qua.");
                }
            }

            // Kiểm tra xem ca đấu đã được đặt hoặc đang hoạt động hay chưa
            boolean isAlreadyBooked = courtReservationRepository
                    .findByCourtIdAndReservationDate(court.getId(), bookingDate).stream()
                    .anyMatch(res -> res.getSlot().getId().equals(slot.getId()) && Boolean.TRUE.equals(res.getIsActive()));

            if (isAlreadyBooked) {
                throw new BadRequestException("Sân [" + court.getName() + "] đã bị đặt trùng lịch vào ca " 
                        + slot.getStartTime() + " - " + slot.getEndTime() + " ngày " + bookingDate);
            }

            // Xác định loại ngày (ngày thường hay cuối tuần)
            // DayOfWeek: 1 (Thứ Hai) đến 7 (Chủ Nhật). Thứ Bảy (6) và Chủ Nhật (7) là cuối tuần
            DayType dayType = (bookingDate.getDayOfWeek().getValue() >= 6) ? DayType.WEEKEND : DayType.WEEKDAY;

            // Lấy quy tắc tính giá tương ứng
            PricingRule pricingRule = pricingRuleRepository
                    .findByCourtIdAndSlotIdAndDayType(court.getId(), slot.getId(), dayType)
                    .orElseThrow(() -> new BadRequestException("Sân [" + court.getName() + "] vào ca " 
                            + slot.getStartTime().toString().substring(0, 5) + " - " + slot.getEndTime().toString().substring(0, 5) 
                            + " ngày " + bookingDate + " chưa được cấu hình giá và không thể đặt lịch."));

            if (com.quy.badmintonbe.common.enums.SlotStatus.INACTIVE.equals(pricingRule.getStatus())) {
                throw new BadRequestException("Sân [" + court.getName() + "] vào ca " 
                        + slot.getStartTime().toString().substring(0, 5) + " - " + slot.getEndTime().toString().substring(0, 5) 
                        + " ngày " + bookingDate + " tạm thời ngưng hoạt động (Khóa ca).");
            }

            BigDecimal courtPrice = pricingRule.getPrice();
            subTotal = subTotal.add(courtPrice);

            BookingDetail detail = BookingDetail.builder()
                    .court(court)
                    .slot(slot)
                    .bookingDate(bookingDate)
                    .unitPrice(courtPrice)
                    .detailStatus("BOOKED")
                    .build();

            bookingDetailsToSave.add(detail);
        }

        // 3. Xử lý các dịch vụ/sản phẩm đi kèm
        List<BookingServiceItem> serviceItemsToSave = new ArrayList<>();
        if (dto.getServices() != null) {
            for (BookingServiceRequest svcDto : dto.getServices()) {
                Product product = productRepository.findById(svcDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ/sản phẩm với ID: " + svcDto.getProductId()));

                if (svcDto.getQuantity() == null || svcDto.getQuantity() <= 0) {
                    throw new BadRequestException("Số lượng dịch vụ/sản phẩm mua/thuê phải lớn hơn 0.");
                }

                BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(svcDto.getQuantity()));
                subTotal = subTotal.add(itemPrice);

                BookingServiceItem svcItem = BookingServiceItem.builder()
                        .product(product)
                        .quantity(svcDto.getQuantity())
                        .unitPrice(product.getPrice())
                        .totalPrice(itemPrice)
                        .build();

                serviceItemsToSave.add(svcItem);
            }
        }

        // 4. Kiểm tra tính hợp lệ và áp dụng mã giảm giá (Voucher)
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (dto.getVoucherId() != null) {
            voucher = voucherRepository.findById(dto.getVoucherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + dto.getVoucherId()));

            if (voucher.getEndDate().isBefore(LocalDateTime.now()) || 
                voucher.getStartDate().isAfter(LocalDateTime.now()) ||
                !com.quy.badmintonbe.common.enums.VoucherStatus.ACTIVE.equals(voucher.getStatus())) {
                throw new BadRequestException("Mã giảm giá đã hết hạn sử dụng hoặc không còn hoạt động.");
            }

            if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new BadRequestException("Mã giảm giá đã đạt giới hạn lượt sử dụng.");
            }

            if (subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new BadRequestException("Tổng giá trị đơn hàng không đạt giá trị tối thiểu để áp dụng mã giảm giá này.");
            }

            if (DiscountType.PERCENT.equals(voucher.getDiscountType())) {
                discountAmount = subTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discountAmount = voucher.getMaxDiscount();
                }
            } else if (DiscountType.AMOUNT.equals(voucher.getDiscountType())) {
                discountAmount = voucher.getDiscountValue();
            }

            // Đảm bảo số tiền giảm giá không vượt quá tổng tiền trước giảm
            if (discountAmount.compareTo(subTotal) > 0) {
                discountAmount = subTotal;
            }

            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }

        BigDecimal totalPrice = subTotal.subtract(discountAmount);

        // 5. Lưu thông tin hóa đơn Booking
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .voucher(voucher)
                .discountAmount(discountAmount)
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 6. Lưu chi tiết đơn đặt và tạo lịch giữ sân
        for (BookingDetail detail : bookingDetailsToSave) {
            detail.setBooking(savedBooking);
            BookingDetail savedDetail = bookingDetailRepository.save(detail);

            // Đăng ký thông tin giữ sân chống trùng
            CourtReservation reservation = CourtReservation.builder()
                    .court(savedDetail.getCourt())
                    .slot(savedDetail.getSlot())
                    .reservationDate(savedDetail.getBookingDate())
                    .sourceType(ReservationSourceType.BOOKING)
                    .sourceId(savedDetail.getId())
                    .status(ReservationStatus.ACTIVE)
                    .isActive(true)
                    .note("Đặt lịch giữ chỗ cho hóa đơn: " + bookingCode)
                    .build();

            courtReservationRepository.save(reservation);
        }

        // 7. Lưu các dịch vụ đi kèm hóa đơn
        for (BookingServiceItem svcItem : serviceItemsToSave) {
            svcItem.setBooking(savedBooking);
            bookingServiceRepository.save(svcItem);
        }

        return mapToResponse(savedBooking);
    }

    @Override
    public BookingResponse updateBooking(Long id, BookingResponse dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + id));

        booking.setTotalPrice(dto.getTotalPrice());
        booking.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getBookingStatus() != null) {
            booking.setBookingStatus(dto.getBookingStatus());
        }
        if (dto.getPaymentStatus() != null) {
            booking.setPaymentStatus(dto.getPaymentStatus());
        }

        if (dto.getVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(dto.getVoucherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + dto.getVoucherId()));
            booking.setVoucher(voucher);
        } else {
            booking.setVoucher(null);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponse(updatedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + id));

        // Guard 1: Không cho hủy đơn Shadow Booking đại diện của Subscription
        if (booking.getBookingCode() != null && booking.getBookingCode().startsWith("BK-SUB-")) {
            throw new IllegalStateException("Không thể hủy hóa đơn đại diện của gói hội viên cố định. Hãy hủy gói đăng ký cố định thay thế.");
        }

        // Guard 2: Không cho hủy đơn đã hoàn thành hoặc đã hủy trước đó
        if (BookingStatus.COMPLETED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Không thể hủy đơn đặt sân đã hoàn thành.");
        }
        if (BookingStatus.CANCELLED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Đơn đặt sân này đã được hủy trước đó.");
        }

        // Guard 3: Không cho hủy nếu TẤT CẢ các ngày đặt sân đã qua
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(id);
        boolean allDatesPassed = !details.isEmpty() && details.stream()
                .allMatch(d -> d.getBookingDate().isBefore(java.time.LocalDate.now()));
        if (allDatesPassed) {
            throw new IllegalStateException("Không thể hủy đơn đặt sân vì tất cả các ngày chơi đã qua.");
        }

        // Thực hiện tính toán hoàn tiền nếu đơn đặt sân đã thanh toán thành công
        if (PaymentStatus.PAID.equals(booking.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(booking.getPaymentStatus())) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);

            // Tìm giao dịch thanh toán thành công
            List<Payment> payments = paymentRepository.findByBookingId(id);
            Payment successfulPayment = payments.stream()
                    .filter(p -> PaymentStatus.PAID.equals(p.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(p.getPaymentStatus()))
                    .findFirst()
                    .orElse(null);

            if (successfulPayment != null) {
                // Xác định giờ chơi của ca sớm nhất trong đơn
                LocalDateTime earliestPlayTime = null;
                for (BookingDetail detail : details) {
                    LocalDateTime pt = LocalDateTime.of(detail.getBookingDate(), detail.getSlot().getStartTime());
                    if (earliestPlayTime == null || pt.isBefore(earliestPlayTime)) {
                        earliestPlayTime = pt;
                    }
                }

                // Tính khoảng thời gian chênh lệch (tiếng) so với hiện tại
                ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
                LocalDateTime now = LocalDateTime.now(vnZone);
                long hoursDiff = earliestPlayTime != null ? java.time.temporal.ChronoUnit.HOURS.between(now, earliestPlayTime) : 0;

                BigDecimal refundPercentage = BigDecimal.ZERO;
                if (hoursDiff >= 24) {
                    refundPercentage = new BigDecimal("100.00");
                } else if (hoursDiff >= 12) {
                    refundPercentage = new BigDecimal("50.00");
                }

                BigDecimal refundAmount = booking.getTotalPrice()
                        .multiply(refundPercentage)
                        .divide(new BigDecimal("100.00"), 2, java.math.RoundingMode.HALF_UP);

                // Ghi nhận bản ghi hoàn tiền
                String finalReason = (reason == null || reason.trim().isEmpty())
                        ? "Hủy lịch đặt sân ca lẻ " + booking.getBookingCode() + " (Hoàn " + refundPercentage + "%)"
                        : reason;

                Refund refund = Refund.builder()
                        .payment(successfulPayment)
                        .refundCode("RF-" + booking.getBookingCode() + "-" + System.currentTimeMillis())
                        .refundAmount(refundAmount)
                        .refundReason(finalReason)
                        .status(PaymentStatus.SUCCESS)
                        .build();

                refundRepository.save(refund);
            }
        } else {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Hủy các lịch giữ sân liên quan trong cơ sở dữ liệu
        for (BookingDetail detail : details) {
            detail.setDetailStatus("CANCELLED");
            bookingDetailRepository.save(detail);

            // Hủy hoạt động giữ sân (sử dụng null để tránh trùng ràng buộc duy nhất)
            List<CourtReservation> reservations = courtReservationRepository
                    .findByCourtIdAndReservationDate(detail.getCourt().getId(), detail.getBookingDate());

            for (CourtReservation res : reservations) {
                if (res.getSlot().getId().equals(detail.getSlot().getId()) &&
                    ReservationSourceType.BOOKING.equals(res.getSourceType()) &&
                    detail.getId().equals(res.getSourceId())) {
                    res.setStatus(ReservationStatus.CANCELLED);
                    res.setIsActive(null); // nhả slot
                    courtReservationRepository.save(res);
                }
            }
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingDetailResponse> detailResponses = bookingDetailRepository.findByBookingId(booking.getId()).stream()
                .map(detail -> BookingDetailResponse.builder()
                        .id(detail.getId())
                        .courtId(detail.getCourt().getId())
                        .courtName(detail.getCourt().getName())
                        .branchName(detail.getCourt().getBranch().getName())
                        .slotId(detail.getSlot().getId())
                        .startTime(detail.getSlot().getStartTime().toString())
                        .endTime(detail.getSlot().getEndTime().toString())
                        .bookingDate(detail.getBookingDate())
                        .unitPrice(detail.getUnitPrice())
                        .detailStatus(detail.getDetailStatus())
                        .build())
                .collect(Collectors.toList());

        List<BookingServiceResponse> serviceResponses = bookingServiceRepository.findByBookingId(booking.getId()).stream()
                .map(item -> BookingServiceResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getId())
                .voucherId(booking.getVoucher() != null ? booking.getVoucher().getId() : null)
                .discountAmount(booking.getDiscountAmount())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .details(detailResponses)
                .services(serviceResponses)
                .build();
    }

    @Override
    public List<Long> getOccupiedSlots(Long courtId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        return courtReservationRepository.findByCourtIdAndReservationDate(courtId, localDate).stream()
                .filter(res -> Boolean.TRUE.equals(res.getIsActive()))
                .map(res -> res.getSlot().getId())
                .collect(Collectors.toList());
    }

    private Booking mapToEntity(BookingResponse dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));

        Voucher voucher = null;
        if (dto.getVoucherId() != null) {
            voucher = voucherRepository.findById(dto.getVoucherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + dto.getVoucherId()));
        }

        return Booking.builder()
                .id(dto.getId())
                .bookingCode(dto.getBookingCode())
                .user(user)
                .voucher(voucher)
                .discountAmount(dto.getDiscountAmount())
                .totalPrice(dto.getTotalPrice())
                .bookingStatus(dto.getBookingStatus() != null ? dto.getBookingStatus() : BookingStatus.PENDING)
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PaymentStatus.UNPAID)
                .build();
    }
}
