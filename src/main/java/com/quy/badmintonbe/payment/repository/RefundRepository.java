package com.quy.badmintonbe.payment.repository;

import com.quy.badmintonbe.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRefundCode(String refundCode);
    List<Refund> findByPaymentId(Long paymentId);
}
