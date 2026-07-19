package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.dto.CancellationPolicyDto;
import com.quy.badmintonbe.booking.entity.CancellationPolicy;
import com.quy.badmintonbe.booking.repository.CancellationPolicyRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancellationPolicyServiceImpl implements CancellationPolicyService {

    private final CancellationPolicyRepository cancellationPolicyRepository;

    @Override
    public List<CancellationPolicyDto> getPoliciesByBranch(Long branchId) {
        return cancellationPolicyRepository.findByBranchId(branchId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CancellationPolicyDto updatePolicy(Long id, CancellationPolicyDto dto) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc hủy với ID: " + id));

        policy.setHoursBefore(dto.getHoursBefore());
        policy.setRefundPercentage(dto.getRefundPercentage());
        
        CancellationPolicy saved = cancellationPolicyRepository.save(policy);
        return mapToDto(saved);
    }

    private CancellationPolicyDto mapToDto(CancellationPolicy policy) {
        return CancellationPolicyDto.builder()
                .id(policy.getId())
                .branchId(policy.getBranch().getId())
                .hoursBefore(policy.getHoursBefore())
                .refundPercentage(policy.getRefundPercentage())
                .status(policy.getStatus().name())
                .build();
    }
}
