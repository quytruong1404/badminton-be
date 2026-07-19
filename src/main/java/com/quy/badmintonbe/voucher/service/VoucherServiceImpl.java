package com.quy.badmintonbe.voucher.service;

import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.voucher.dto.VoucherDto;
import com.quy.badmintonbe.voucher.entity.Voucher;
import com.quy.badmintonbe.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public VoucherDto getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
        return mapToDto(voucher);
    }

    @Override
    public VoucherDto getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với mã: " + code));
        return mapToDto(voucher);
    }

    @Override
    public List<VoucherDto> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDto createVoucher(VoucherDto dto) {
        Voucher voucher = mapToEntity(dto);
        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    @Override
    public VoucherDto updateVoucher(Long id, VoucherDto dto) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));

        voucher.setCode(dto.getCode());
        voucher.setDiscountType(dto.getDiscountType());
        voucher.setDiscountValue(dto.getDiscountValue());
        voucher.setMinOrderValue(dto.getMinOrderValue());
        voucher.setMaxDiscount(dto.getMaxDiscount());
        voucher.setUsageLimit(dto.getUsageLimit());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) {
            voucher.setStatus(dto.getStatus());
        }

        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
        voucherRepository.delete(voucher);
    }

    private VoucherDto mapToDto(Voucher voucher) {
        return VoucherDto.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscount(voucher.getMaxDiscount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus())
                .build();
    }

    private Voucher mapToEntity(VoucherDto dto) {
        return Voucher.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue())
                .maxDiscount(dto.getMaxDiscount())
                .usageLimit(dto.getUsageLimit())
                .usedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : com.quy.badmintonbe.common.enums.VoucherStatus.ACTIVE)
                .build();
    }
}
