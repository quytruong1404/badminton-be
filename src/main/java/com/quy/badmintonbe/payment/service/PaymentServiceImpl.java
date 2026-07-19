package com.quy.badmintonbe.payment.service;

import com.quy.badmintonbe.booking.entity.Booking;
import com.quy.badmintonbe.booking.repository.BookingRepository;
import com.quy.badmintonbe.common.config.VNPayConfig;
import com.quy.badmintonbe.common.enums.PaymentMethod;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import com.quy.badmintonbe.common.enums.BookingStatus;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.payment.dto.PaymentDto;
import com.quy.badmintonbe.payment.entity.Payment;
import com.quy.badmintonbe.payment.repository.PaymentRepository;
import com.quy.badmintonbe.subscription.entity.Subscription;
import com.quy.badmintonbe.subscription.repository.SubscriptionRepository;
import com.quy.badmintonbe.common.enums.SubscriptionStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán với ID: " + id));
        return mapToDto(payment);
    }

    @Override
    public PaymentDto getPaymentByTransactionCode(String transactionCode) {
        Payment payment = paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán với mã giao dịch: " + transactionCode));
        return mapToDto(payment);
    }

    @Override
    public List<PaymentDto> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto createPayment(PaymentDto dto) {
        Payment payment = mapToEntity(dto);
        Payment savedPayment = paymentRepository.save(payment);
        return mapToDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto processPaymentCallback(String transactionCode, String gatewayTransactionId, boolean success) {
        Payment payment = paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán với mã giao dịch: " + transactionCode));

        if (success) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPayDate(LocalDateTime.now());
            // Cập nhật trạng thái đơn đặt sân
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Nếu đây là Booking hóa đơn của Subscription, cập nhật Subscription sang ACTIVE
            if (booking.getBookingCode() != null && booking.getBookingCode().startsWith("BK-SUB-")) {
                try {
                    Long subscriptionId = Long.parseLong(booking.getBookingCode().replace("BK-SUB-", ""));
                    Subscription sub = subscriptionRepository.findById(subscriptionId).orElse(null);
                    if (sub != null) {
                        sub.setStatus(SubscriptionStatus.ACTIVE);
                        subscriptionRepository.save(sub);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật trạng thái Subscription từ Booking Code: " + e.getMessage());
                }
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);

            // Nếu thanh toán thất bại, cập nhật Subscription sang CANCELLED
            if (booking.getBookingCode() != null && booking.getBookingCode().startsWith("BK-SUB-")) {
                try {
                    Long subscriptionId = Long.parseLong(booking.getBookingCode().replace("BK-SUB-", ""));
                    Subscription sub = subscriptionRepository.findById(subscriptionId).orElse(null);
                    if (sub != null) {
                        sub.setStatus(SubscriptionStatus.CANCELLED);
                        subscriptionRepository.save(sub);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi hủy Subscription từ Booking Code: " + e.getMessage());
                }
            }
        }

        payment.setGatewayTransactionId(gatewayTransactionId);
        Payment updatedPayment = paymentRepository.save(payment);
        return mapToDto(updatedPayment);
    }

    @Override
    @Transactional
    public String createVNPayUrl(Long bookingId, HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + bookingId));

        // Tạo bản ghi giao dịch thanh toán ở trạng thái PENDING chờ xử lý
        String transactionCode = "VNP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(PaymentMethod.VNPAY)
                .amount(booking.getTotalPrice())
                .transactionCode(transactionCode)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don dat san: " + booking.getBookingCode();
        String vnp_OrderType = "other";
        String vnp_TxnRef = transactionCode;
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        // Nhân số tiền với 100 theo đúng định dạng yêu cầu của VNPay
        long amount = booking.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnp_Params = new java.util.HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Định dạng ngày giờ giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Tạo chuỗi tham số để băm mã hóa chữ ký bảo mật và tạo chuỗi truy vấn
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        try {
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Tạo dữ liệu để băm mã hóa bảo mật
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Tạo dữ liệu truy vấn
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa các tham số thanh toán", e);
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNPayConfig.vnp_Url + "?" + queryUrl;
    }

    @Override
    @Transactional
    public PaymentDto confirmMockPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán với ID: " + paymentId));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayDate(LocalDateTime.now());
        payment.setGatewayTransactionId("MOCK-GATEWAY-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Booking booking = payment.getBooking();
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Nếu đây là Booking hóa đơn của Subscription, cập nhật Subscription sang ACTIVE
        if (booking.getBookingCode() != null && booking.getBookingCode().startsWith("BK-SUB-")) {
            try {
                Long subscriptionId = Long.parseLong(booking.getBookingCode().replace("BK-SUB-", ""));
                Subscription sub = subscriptionRepository.findById(subscriptionId).orElse(null);
                if (sub != null) {
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(sub);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật trạng thái Subscription từ Booking Code: " + e.getMessage());
            }
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToDto(updatedPayment);
    }

    private PaymentDto mapToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .transactionCode(payment.getTransactionCode())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .paymentStatus(payment.getPaymentStatus())
                .rawResponse(payment.getRawResponse())
                .payDate(payment.getPayDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private Payment mapToEntity(PaymentDto dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + dto.getBookingId()));

        return Payment.builder()
                .id(dto.getId())
                .booking(booking)
                .paymentMethod(dto.getPaymentMethod())
                .amount(dto.getAmount())
                .transactionCode(dto.getTransactionCode())
                .gatewayTransactionId(dto.getGatewayTransactionId())
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PaymentStatus.PENDING)
                .rawResponse(dto.getRawResponse())
                .payDate(dto.getPayDate())
                .build();
    }
}
