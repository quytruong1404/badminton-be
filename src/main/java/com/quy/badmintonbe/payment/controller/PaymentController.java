package com.quy.badmintonbe.payment.controller;

import com.quy.badmintonbe.common.config.VNPayConfig;
import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.payment.dto.PaymentDto;
import com.quy.badmintonbe.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        ApiResponse<PaymentDto> response = ApiResponse.<PaymentDto>builder()
                .success(true)
                .message("Lấy thông tin giao dịch thành công")
                .data(payment)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPayments(
            @RequestParam(required = false) Long bookingId) {
        List<PaymentDto> payments;
        if (bookingId != null) {
            payments = paymentService.getPaymentsByBookingId(bookingId);
        } else {
            payments = paymentService.getAllPayments();
        }
        ApiResponse<List<PaymentDto>> response = ApiResponse.<List<PaymentDto>>builder()
                .success(true)
                .message("Lấy danh sách giao dịch thành công")
                .data(payments)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@RequestBody PaymentDto paymentDto) {
        PaymentDto createdPayment = paymentService.createPayment(paymentDto);
        ApiResponse<PaymentDto> response = ApiResponse.<PaymentDto>builder()
                .success(true)
                .message("Tạo giao dịch thành công")
                .data(createdPayment)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 1. Khởi tạo đường dẫn thanh toán VNPay
    @GetMapping("/create-vnpay/{bookingId}")
    public ResponseEntity<ApiResponse<String>> createVNPayUrl(
            @PathVariable Long bookingId,
            HttpServletRequest request) {
        String paymentUrl = paymentService.createVNPayUrl(bookingId, request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Tạo liên kết thanh toán VNPay thành công")
                .data(paymentUrl)
                .build();
        return ResponseEntity.ok(response);
    }

    // 2. Tiếp nhận kết quả callback từ VNPay
    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<PaymentDto>> vnpayCallback(
            @RequestParam Map<String, String> allRequestParams) {
        
        Map<String, String> fields = new HashMap<>(allRequestParams);
        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // Xác thực chữ ký bảo mật từ cổng VNPay
        String signValue = VNPayConfig.hashAllFields(fields);
        boolean isSignatureValid = signValue.equals(vnp_SecureHash);

        String transactionCode = allRequestParams.get("vnp_TxnRef");
        String gatewayTransactionId = allRequestParams.get("vnp_TransactionNo");
        String responseCode = allRequestParams.get("vnp_ResponseCode");

        // Mã phản hồi thành công của VNPay là "00"
        boolean success = isSignatureValid && "00".equals(responseCode);
        
        PaymentDto paymentDto = paymentService.processPaymentCallback(transactionCode, gatewayTransactionId, success);

        ApiResponse<PaymentDto> response = ApiResponse.<PaymentDto>builder()
                .success(success)
                .message(success ? "Thanh toán VNPay thành công!" : "Thanh toán VNPay thất bại.")
                .data(paymentDto)
                .build();
        return ResponseEntity.ok(response);
    }

    // 3. Giả lập xác nhận thanh toán cho khách hàng
    @PostMapping("/{id}/confirm-mock")
    public ResponseEntity<ApiResponse<PaymentDto>> confirmMockPayment(@PathVariable Long id) {
        PaymentDto payment = paymentService.confirmMockPayment(id);
        ApiResponse<PaymentDto> response = ApiResponse.<PaymentDto>builder()
                .success(true)
                .message("Xác nhận chuyển khoản/MOMO đã thành công (giả lập)")
                .data(payment)
                .build();
        return ResponseEntity.ok(response);
    }
}
