package com.quy.badmintonbe.voucher.service;

import com.quy.badmintonbe.voucher.dto.VoucherDto;
import java.util.List;

public interface VoucherService {
    VoucherDto getVoucherById(Long id);
    VoucherDto getVoucherByCode(String code);
    List<VoucherDto> getAllVouchers();
    VoucherDto createVoucher(VoucherDto voucherDto);
    VoucherDto updateVoucher(Long id, VoucherDto voucherDto);
    void deleteVoucher(Long id);
}
