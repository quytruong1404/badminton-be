package com.quy.badmintonbe.payment.service;

import com.quy.badmintonbe.payment.dto.PaymentDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface PaymentService {
    PaymentDto getPaymentById(Long id);
    PaymentDto getPaymentByTransactionCode(String transactionCode);
    List<PaymentDto> getPaymentsByBookingId(Long bookingId);
    List<PaymentDto> getAllPayments();
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto processPaymentCallback(String transactionCode, String gatewayTransactionId, boolean success);
    String createVNPayUrl(Long bookingId, HttpServletRequest request);
    PaymentDto confirmMockPayment(Long paymentId);
}
