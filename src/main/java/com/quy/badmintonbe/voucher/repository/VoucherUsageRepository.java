package com.quy.badmintonbe.voucher.repository;

import com.quy.badmintonbe.voucher.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    List<VoucherUsage> findByUserId(Long userId);
    List<VoucherUsage> findByVoucherId(Long voucherId);
}
