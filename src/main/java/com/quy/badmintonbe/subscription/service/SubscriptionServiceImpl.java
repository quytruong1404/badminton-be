package com.quy.badmintonbe.subscription.service;

import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.common.enums.SubscriptionStatus;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.subscription.dto.SubscriptionDto;
import com.quy.badmintonbe.subscription.dto.SubscriptionScheduleDto;
import com.quy.badmintonbe.subscription.entity.Subscription;
import com.quy.badmintonbe.subscription.entity.SubscriptionSchedule;
import com.quy.badmintonbe.subscription.repository.SubscriptionRepository;
import com.quy.badmintonbe.subscription.repository.SubscriptionScheduleRepository;
import com.quy.badmintonbe.booking.entity.CourtReservation;
import com.quy.badmintonbe.booking.repository.CourtReservationRepository;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.user.entity.User;
import com.quy.badmintonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.entity.BookingDetail;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.enums.PaymentStatus;

import com.quy.badmintonbe.voucher.repository.VoucherRepository;
import com.quy.badmintonbe.payment.repository.PaymentRepository;
import com.quy.badmintonbe.payment.repository.RefundRepository;
import com.quy.badmintonbe.payment.entity.Payment;
import com.quy.badmintonbe.payment.entity.Refund;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final SubscriptionScheduleRepository subscriptionScheduleRepository;
    private final CourtReservationRepository courtReservationRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    public SubscriptionDto getSubscriptionById(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký lịch cố định với ID: " + id));
        return mapToDto(sub);
    }

    @Override
    public SubscriptionDto getSubscriptionByCode(String code) {
        Subscription sub = subscriptionRepository.findBySubscriptionCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký lịch cố định với mã: " + code));
        return mapToDto(sub);
    }

    @Override
    public List<SubscriptionDto> getSubscriptionsByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionDto createSubscription(SubscriptionDto dto) {
        Subscription sub = mapToEntity(dto);
        if (sub.getSubscriptionCode() == null || sub.getSubscriptionCode().isEmpty()) {
            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalDate today = LocalDate.now(vietnamZone);
            String dateStr = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            long countToday = subscriptionRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            sub.setSubscriptionCode(String.format("SUB-%s-%04d", dateStr, countToday + 1));
        }
        Subscription savedSub = subscriptionRepository.save(sub);

        if (dto.getSchedules() != null) {
            for (SubscriptionScheduleDto schedDto : dto.getSchedules()) {
                com.quy.badmintonbe.court.entity.Court court = courtRepository.findById(schedDto.getCourtId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân với ID: " + schedDto.getCourtId()));
                com.quy.badmintonbe.court.entity.TimeSlot slot = timeSlotRepository.findById(schedDto.getSlotId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + schedDto.getSlotId()));

                Branch branch = court.getBranch();
                if (branch != null) {
                    java.time.LocalTime startTime = slot.getStartTime();
                    java.time.LocalTime endTime = slot.getEndTime();
                    java.time.LocalTime openTime = branch.getOpenTime();
                    java.time.LocalTime closeTime = branch.getCloseTime();

                    if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                        throw new com.quy.badmintonbe.common.exception.BadRequestException(
                            "Ca chơi " + startTime.toString().substring(0, 5) 
                            + " - " + endTime.toString().substring(0, 5) 
                            + " nằm ngoài giờ hoạt động của chi nhánh " + branch.getName() 
                            + " (" + openTime.toString().substring(0, 5) + " - " + closeTime.toString().substring(0, 5) + ")."
                        );
                    }
                }

                SubscriptionSchedule sched = SubscriptionSchedule.builder()
                        .subscription(savedSub)
                        .court(court)
                        .slot(slot)
                        .dayOfWeek(schedDto.getDayOfWeek() + 1)
                        .status(com.quy.badmintonbe.common.enums.SlotStatus.ACTIVE)
                        .build();

                subscriptionScheduleRepository.save(sched);

                // Tự động tạo lịch giữ sân (CourtReservation) trong khoảng từ ngày bắt đầu đến ngày kết thúc
                LocalDate start = savedSub.getStartDate();
                LocalDate end = savedSub.getEndDate();
                LocalDate curr = start;

                while (!curr.isAfter(end)) {
                    if (curr.getDayOfWeek().getValue() == schedDto.getDayOfWeek()) {
                        CourtReservation res = CourtReservation.builder()
                                .court(court)
                                .slot(slot)
                                .reservationDate(curr)
                                .sourceType(com.quy.badmintonbe.common.enums.ReservationSourceType.SUBSCRIPTION)
                                .sourceId(savedSub.getId())
                                .status(com.quy.badmintonbe.common.enums.ReservationStatus.ACTIVE)
                                .isActive(true)
                                .note("Đặt cố định: " + savedSub.getSubscriptionCode())
                                .build();
                        courtReservationRepository.save(res);
                    }
                    curr = curr.plusDays(1);
                }
            }
        }

        // Tự động tạo một Booking hóa đơn đại diện cho Subscription để thực hiện thanh toán
        if (dto.getSchedules() != null && !dto.getSchedules().isEmpty()) {
            SubscriptionScheduleDto firstSched = dto.getSchedules().get(0);
            com.quy.badmintonbe.court.entity.Court court = courtRepository.findById(firstSched.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân với ID: " + firstSched.getCourtId()));
            com.quy.badmintonbe.court.entity.TimeSlot slot = timeSlotRepository.findById(firstSched.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + firstSched.getSlotId()));

            Booking shadowBooking = Booking.builder()
                    .bookingCode("BK-SUB-" + savedSub.getId())
                    .user(savedSub.getUser())
                    .voucher(dto.getVoucherId() != null ? voucherRepository.findById(dto.getVoucherId()).orElse(null) : null)
                    .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                    .totalPrice(savedSub.getTotalPrice())
                    .bookingStatus(BookingStatus.PENDING)
                    .paymentStatus(PaymentStatus.UNPAID)
                    .build();

            Booking savedBooking = bookingRepository.save(shadowBooking);

            // Tạo chi tiết hóa đơn tượng trưng để thỏa mãn điều kiện details khác rỗng
            BookingDetail shadowDetail = BookingDetail.builder()
                    .booking(savedBooking)
                    .court(court)
                    .slot(slot)
                    .bookingDate(savedSub.getStartDate())
                    .unitPrice(savedSub.getTotalPrice())
                    .detailStatus("BOOKED")
                    .build();

            bookingDetailRepository.save(shadowDetail);
        }

        return mapToDto(savedSub);
    }

    @Override
    public SubscriptionDto updateSubscription(Long id, SubscriptionDto dto) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký lịch cố định với ID: " + id));

        sub.setStartDate(dto.getStartDate());
        sub.setEndDate(dto.getEndDate());
        sub.setTotalPrice(dto.getTotalPrice());
        if (dto.getStatus() != null) {
            sub.setStatus(dto.getStatus());
        }

        if (dto.getUserId() != null && !sub.getUser().getId().equals(dto.getUserId())) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));
            sub.setUser(user);
        }

        if (dto.getBranchId() != null && !sub.getBranch().getId().equals(dto.getBranchId())) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getBranchId()));
            sub.setBranch(branch);
        }

        Subscription updatedSub = subscriptionRepository.save(sub);
        return mapToDto(updatedSub);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long id, String reason) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký lịch cố định với ID: " + id));
        
        if (SubscriptionStatus.CANCELLED.equals(sub.getStatus())) {
            throw new IllegalStateException("Đăng ký lịch cố định này đã được hủy trước đó.");
        }

        if (sub.getEndDate() != null && sub.getEndDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalStateException("Không thể hủy gói đăng ký lịch cố định đã hết hiệu lực.");
        }

        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);

        // Hủy các lịch giữ sân liên quan
        List<CourtReservation> reservations = courtReservationRepository
                .findBySourceTypeAndSourceId(com.quy.badmintonbe.common.enums.ReservationSourceType.SUBSCRIPTION, id);
        for (CourtReservation res : reservations) {
            res.setStatus(com.quy.badmintonbe.common.enums.ReservationStatus.CANCELLED);
            res.setIsActive(null);
            courtReservationRepository.save(res);
        }

        // Xử lý hoàn trả tiền & ghi nhận bản ghi refunds
        Optional<Booking> optBooking = bookingRepository.findByBookingCode("BK-SUB-" + id);
        if (optBooking.isPresent()) {
            Booking booking = optBooking.get();
            booking.setBookingStatus(BookingStatus.CANCELLED);

            if (PaymentStatus.PAID.equals(booking.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(booking.getPaymentStatus())) {
                booking.setPaymentStatus(PaymentStatus.REFUNDED);

                // Tìm giao dịch thanh toán thành công của hóa đơn đại diện
                List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
                Payment successfulPayment = payments.stream()
                        .filter(p -> PaymentStatus.PAID.equals(p.getPaymentStatus()) || PaymentStatus.SUCCESS.equals(p.getPaymentStatus()))
                        .findFirst()
                        .orElse(null);

                if (successfulPayment != null) {
                    long totalReservations = reservations.size();
                    long futureReservations = reservations.stream()
                            .filter(res -> !res.getReservationDate().isBefore(java.time.LocalDate.now()))
                            .count();

                    // Xác định tỷ lệ hoàn tiền: nếu ngày hủy diễn ra trước ngày bắt đầu gói chơi thì hoàn 100%, ngược lại phạt 20% (hoàn 80%)
                    BigDecimal refundPercentage = java.time.LocalDate.now().isBefore(sub.getStartDate())
                            ? new BigDecimal("100.00")
                            : new BigDecimal("80.00");

                    BigDecimal refundRatio = totalReservations > 0
                            ? new BigDecimal(futureReservations).divide(new BigDecimal(totalReservations), 4, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    BigDecimal refundAmount = sub.getTotalPrice()
                            .multiply(refundRatio)
                            .multiply(refundPercentage)
                            .divide(new BigDecimal("100.00"), 2, java.math.RoundingMode.HALF_UP);

                    // Tạo bản ghi hoàn tiền với lý do do khách chọn
                    String finalReason = (reason == null || reason.trim().isEmpty())
                            ? "Hủy lịch đăng ký cố định " + sub.getSubscriptionCode() + " (Hoàn tiền " + refundPercentage + "% cho " + futureReservations + "/" + totalReservations + " ca chưa chơi)"
                            : reason;

                    Refund refund = Refund.builder()
                            .payment(successfulPayment)
                            .refundCode("RF-SUB-" + sub.getSubscriptionCode() + "-" + System.currentTimeMillis())
                            .refundAmount(refundAmount)
                            .refundReason(finalReason)
                            .status(PaymentStatus.SUCCESS)
                            .build();

                    refundRepository.save(refund);
                }
            } else {
                booking.setPaymentStatus(PaymentStatus.UNPAID);
            }
            bookingRepository.save(booking);
        }
    }

    private SubscriptionDto mapToDto(Subscription sub) {
        List<SubscriptionScheduleDto> schedDtos = subscriptionScheduleRepository.findBySubscriptionId(sub.getId()).stream()
                .map(sched -> SubscriptionScheduleDto.builder()
                        .id(sched.getId())
                        .courtId(sched.getCourt().getId())
                        .courtName(sched.getCourt().getName())
                        .slotId(sched.getSlot().getId())
                        .startTime(sched.getSlot().getStartTime().toString())
                        .endTime(sched.getSlot().getEndTime().toString())
                        .dayOfWeek(sched.getDayOfWeek() - 1)
                        .status(sched.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return SubscriptionDto.builder()
                .id(sub.getId())
                .subscriptionCode(sub.getSubscriptionCode())
                .userId(sub.getUser().getId())
                .branchId(sub.getBranch().getId())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .totalPrice(sub.getTotalPrice())
                .status(sub.getStatus())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .schedules(schedDtos)
                .build();
    }

    private Subscription mapToEntity(SubscriptionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + dto.getUserId()));
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getBranchId()));

        return Subscription.builder()
                .id(dto.getId())
                .subscriptionCode(dto.getSubscriptionCode())
                .user(user)
                .branch(branch)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalPrice(dto.getTotalPrice())
                .status(dto.getStatus() != null ? dto.getStatus() : SubscriptionStatus.PENDING)
                .build();
    }
}
