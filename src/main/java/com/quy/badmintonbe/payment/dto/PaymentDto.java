package com.quy.badmintonbe.payment.dto;

import com.quy.badmintonbe.common.enums.PaymentMethod;
import com.quy.badmintonbe.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private Long bookingId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String transactionCode;
    private String gatewayTransactionId;
    private PaymentStatus paymentStatus;
    private String rawResponse;
    private LocalDateTime payDate;
    private LocalDateTime createdAt;
}
