package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.dto.CancellationPolicyDto;

import java.util.List;

public interface CancellationPolicyService {
    List<CancellationPolicyDto> getPoliciesByBranch(Long branchId);
    CancellationPolicyDto updatePolicy(Long id, CancellationPolicyDto dto);
}
