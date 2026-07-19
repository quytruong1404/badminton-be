package com.quy.badmintonbe.voucher.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.voucher.dto.VoucherDto;
import com.quy.badmintonbe.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherDto>> getVoucherById(@PathVariable Long id) {
        VoucherDto voucher = voucherService.getVoucherById(id);
        ApiResponse<VoucherDto> response = ApiResponse.<VoucherDto>builder()
                .success(true)
                .message("Voucher retrieved successfully")
                .data(voucher)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code")
    public ResponseEntity<ApiResponse<VoucherDto>> getVoucherByCode(@RequestParam String code) {
        VoucherDto voucher = voucherService.getVoucherByCode(code);
        ApiResponse<VoucherDto> response = ApiResponse.<VoucherDto>builder()
                .success(true)
                .message("Voucher retrieved successfully")
                .data(voucher)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getAllVouchers() {
        List<VoucherDto> vouchers = voucherService.getAllVouchers();
        ApiResponse<List<VoucherDto>> response = ApiResponse.<List<VoucherDto>>builder()
                .success(true)
                .message("Vouchers retrieved successfully")
                .data(vouchers)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherDto>> createVoucher(@RequestBody VoucherDto voucherDto) {
        VoucherDto createdVoucher = voucherService.createVoucher(voucherDto);
        ApiResponse<VoucherDto> response = ApiResponse.<VoucherDto>builder()
                .success(true)
                .message("Voucher created successfully")
                .data(createdVoucher)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherDto>> updateVoucher(
            @PathVariable Long id, @RequestBody VoucherDto voucherDto) {
        VoucherDto updatedVoucher = voucherService.updateVoucher(id, voucherDto);
        ApiResponse<VoucherDto> response = ApiResponse.<VoucherDto>builder()
                .success(true)
                .message("Voucher updated successfully")
                .data(updatedVoucher)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Voucher deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
