package com.quy.badmintonbe.booking.controller;

import com.quy.badmintonbe.booking.dto.CancellationPolicyDto;
import com.quy.badmintonbe.booking.service.CancellationPolicyService;
import com.quy.badmintonbe.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cancellation-policies")
@RequiredArgsConstructor
public class CancellationPolicyController {

    private final CancellationPolicyService cancellationPolicyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CancellationPolicyDto>>> getPoliciesByBranch(@RequestParam Long branchId) {
        List<CancellationPolicyDto> policies = cancellationPolicyService.getPoliciesByBranch(branchId);
        ApiResponse<List<CancellationPolicyDto>> response = ApiResponse.<List<CancellationPolicyDto>>builder()
                .success(true)
                .message("Cancellation policies retrieved successfully")
                .data(policies)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CancellationPolicyDto>> updatePolicy(
            @PathVariable Long id, @RequestBody CancellationPolicyDto dto) {
        CancellationPolicyDto updated = cancellationPolicyService.updatePolicy(id, dto);
        ApiResponse<CancellationPolicyDto> response = ApiResponse.<CancellationPolicyDto>builder()
                .success(true)
                .message("Cancellation policy updated successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }
}
