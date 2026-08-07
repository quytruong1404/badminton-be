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
import com.quy.badmintonbe.booking.repository.CancellationPolicyRepository;
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
import com.quy.badmintonbe.product.repository.BranchInventoryRepository;
import com.quy.badmintonbe.booking.repository.BookingServiceItemRepository;
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
import com.quy.badmintonbe.product.service.BranchInventoryService;
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
    private final BranchInventoryService branchInventoryService;
    private final BranchInventoryRepository branchInventoryRepository;
    private final BookingServiceItemRepository bookingServiceItemRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;

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
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));

        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(vietnamZone);
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        long countToday = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        String bookingCode = String.format("BK-%s-%04d", dateStr, countToday + 1);

        BigDecimal subTotal = BigDecimal.ZERO;

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
                if (com.quy.badmintonbe.common.enums.BranchStatus.MAINTENANCE.equals(branch.getStatus())) {
                    throw new BadRequestException("Chi nhánh [" + branch.getName() + "] hiện đang bảo trì, tạm thời ngưng nhận đặt sân.");
                }
                if (com.quy.badmintonbe.common.enums.BranchStatus.CLOSED.equals(branch.getStatus())) {
                    throw new BadRequestException("Chi nhánh [" + branch.getName() + "] hiện đang tạm đóng cửa.");
                }

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

            if (com.quy.badmintonbe.common.enums.CourtStatus.MAINTENANCE.equals(court.getStatus())) {
                throw new BadRequestException("Sân [" + court.getName() + "] hiện đang trong thời gian bảo trì.");
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

            boolean isAlreadyBooked = courtReservationRepository
                    .findByCourtIdAndReservationDate(court.getId(), bookingDate).stream()
                    .anyMatch(res -> res.getSlot() != null && res.getSlot().getId().equals(slot.getId()) && Boolean.TRUE.equals(res.getIsActive()));

            if (isAlreadyBooked) {
                throw new BadRequestException("Sân [" + court.getName() + "] đã bị đặt trùng lịch vào ca " 
                        + slot.getStartTime() + " - " + slot.getEndTime() + " ngày " + bookingDate);
            }

            DayType dayType = (bookingDate.getDayOfWeek().getValue() >= 6) ? DayType.WEEKEND : DayType.WEEKDAY;

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

        List<BookingServiceItem> serviceItemsToSave = new ArrayList<>();
        if (dto.getServices() != null && !dto.getServices().isEmpty()) {
            Long bookingBranchId = bookingDetailsToSave.get(0).getCourt().getBranch().getId();
            for (BookingServiceRequest svcDto : dto.getServices()) {
                Product product = productRepository.findById(svcDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ/sản phẩm với ID: " + svcDto.getProductId()));

                if (svcDto.getQuantity() == null || svcDto.getQuantity() <= 0) {
                    throw new BadRequestException("Số lượng dịch vụ/sản phẩm mua/thuê phải lớn hơn 0.");
                }

                if (com.quy.badmintonbe.common.enums.ProductStatus.INACTIVE.equals(product.getStatus())) {
                    throw new BadRequestException("Sản phẩm/Dịch vụ [" + product.getName() + "] hiện đang ngưng cung cấp.");
                }

                if (com.quy.badmintonbe.common.enums.ProductType.RENT.equals(product.getProductType())) {
                    List<com.quy.badmintonbe.product.entity.BranchInventory> inventories = branchInventoryRepository.findAllByBranchIdAndProductId(bookingBranchId, product.getId());
                    int totalBranchStock = (!inventories.isEmpty() && inventories.get(0).getQuantity() != null) ? inventories.get(0).getQuantity() : 0;

                    for (BookingDetail detail : bookingDetailsToSave) {
                        Integer rentedInSlot = bookingServiceItemRepository.countRentedQuantityInSlot(
                                product.getId(), detail.getBookingDate(), detail.getSlot().getId(),
                                List.of("PENDING", "CONFIRMED", "COMPLETED")
                        );
                        int alreadyRented = rentedInSlot != null ? rentedInSlot : 0;
                        int availableInSlot = totalBranchStock - alreadyRented;

                        if (svcDto.getQuantity() > availableInSlot) {
                            throw new BadRequestException("Dụng cụ [" + product.getName() + "] tại chi nhánh chỉ còn " 
                                    + Math.max(0, availableInSlot) + " " + product.getUnit() + " khả dụng trong ca " 
                                    + detail.getSlot().getStartTime().toString().substring(0, 5) + " - " 
                                    + detail.getSlot().getEndTime().toString().substring(0, 5) + " ngày " 
                                    + detail.getBookingDate() + " (Đã có khách khác đặt thuê " + alreadyRented + " " + product.getUnit() + ").");
                        }
                    }
                } else {
                    branchInventoryService.deductStock(bookingBranchId, product.getId(), svcDto.getQuantity());
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

            if (discountAmount.compareTo(subTotal) > 0) {
                discountAmount = subTotal;
            }

            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }

        BigDecimal totalPrice = subTotal.subtract(discountAmount);

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

        for (BookingDetail detail : bookingDetailsToSave) {
            detail.setBooking(savedBooking);
            BookingDetail savedDetail = bookingDetailRepository.save(detail);

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

        if (booking.getBookingCode() != null && booking.getBookingCode().startsWith("BK-SUB-")) {
            throw new IllegalStateException("Không thể hủy hóa đơn đại diện của gói hội viên cố định. Hãy hủy gói đăng ký cố định thay thế.");
        }

        if (BookingStatus.COMPLETED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Không thể hủy đơn đặt sân đã hoàn thành.");
        }
        if (BookingStatus.CANCELLED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Đơn đặt sân này đã được hủy trước đó.");
        }

        List<BookingDetail> details = bookingDetailRepository.findByBookingId(id);
        boolean allDatesPassed = !details.isEmpty() && details.stream()
                .allMatch(d -> d.getBookingDate().isBefore(java.time.LocalDate.now()));
        if (allDatesPassed) {
            throw new IllegalStateException("Không thể hủy đơn đặt sân vì tất cả các ngày chơi đã qua.");
        }

        if (PaymentStatus.PAID.equals(booking.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(booking.getPaymentStatus())) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);

            List<Payment> payments = paymentRepository.findByBookingId(id);
            Payment successfulPayment = payments.stream()
                    .filter(p -> PaymentStatus.PAID.equals(p.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(p.getPaymentStatus()))
                    .findFirst()
                    .orElse(null);

            if (successfulPayment != null) {
                
                LocalDateTime earliestPlayTime = null;
                for (BookingDetail detail : details) {
                    LocalDateTime pt = LocalDateTime.of(detail.getBookingDate(), detail.getSlot().getStartTime());
                    if (earliestPlayTime == null || pt.isBefore(earliestPlayTime)) {
                        earliestPlayTime = pt;
                    }
                }

                ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
                LocalDateTime now = LocalDateTime.now(vnZone);
                long hoursDiff = earliestPlayTime != null ? java.time.temporal.ChronoUnit.HOURS.between(now, earliestPlayTime) : 0;

                Long bookingBranchId = details.get(0).getCourt().getBranch().getId();
                List<com.quy.badmintonbe.booking.entity.CancellationPolicy> policies = cancellationPolicyRepository.findByBranchId(bookingBranchId);
                policies.sort((p1, p2) -> Integer.compare(p2.getHoursBefore(), p1.getHoursBefore()));

                BigDecimal refundPercentage = BigDecimal.ZERO;
                for (com.quy.badmintonbe.booking.entity.CancellationPolicy policy : policies) {
                    if (hoursDiff >= policy.getHoursBefore()) {
                        refundPercentage = policy.getRefundPercentage();
                        break;
                    }
                }

                BigDecimal refundAmount = booking.getTotalPrice()
                        .multiply(refundPercentage)
                        .divide(new BigDecimal("100.00"), 2, java.math.RoundingMode.HALF_UP);

                String finalReason = (reason == null || reason.trim().isEmpty())
                        ? "Hủy lịch đặt sân ca lẻ " + booking.getBookingCode() + " (Hoàn " + refundPercentage + "%)"
                        : reason;

                Refund refund = Refund.builder()
                        .payment(successfulPayment)
                        .refundCode("RF-" + booking.getBookingCode() + "-" + System.currentTimeMillis())
                        .refundAmount(refundAmount)
                        .refundReason(finalReason)
                        .gatewayRefundId("VNP-RF-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000))
                        .status("SUCCESS")
                        .build();

                refundRepository.save(refund);
            }
        } else {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        for (BookingDetail detail : details) {
            detail.setDetailStatus("CANCELLED");
            bookingDetailRepository.save(detail);

            List<CourtReservation> reservations = courtReservationRepository
                    .findByCourtIdAndReservationDate(detail.getCourt().getId(), detail.getBookingDate());

            for (CourtReservation res : reservations) {
                if (res.getSlot().getId().equals(detail.getSlot().getId()) &&
                    ReservationSourceType.BOOKING.equals(res.getSourceType()) &&
                    detail.getId().equals(res.getSourceId())) {
                    res.setStatus(ReservationStatus.CANCELLED);
                    res.setIsActive(null); 
                    courtReservationRepository.save(res);
                }
            }
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingDetailResponse> detailResponses = bookingDetailRepository.findByBookingId(booking.getId()).stream()
                .filter(detail -> detail.getSlot() != null && detail.getCourt() != null)
                .map(detail -> BookingDetailResponse.builder()
                        .id(detail.getId())
                        .courtId(detail.getCourt().getId())
                        .courtName(detail.getCourt().getName())
                        .branchName(detail.getCourt().getBranch() != null ? detail.getCourt().getBranch().getName() : null)
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
                .filter(res -> Boolean.TRUE.equals(res.getIsActive()) && res.getSlot() != null)
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
