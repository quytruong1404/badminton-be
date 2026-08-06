package com.quy.badmintonbe.booking.service;

import com.quy.badmintonbe.booking.dto.CancellationPolicyDto;
import com.quy.badmintonbe.booking.entity.CancellationPolicy;
import com.quy.badmintonbe.booking.repository.CancellationPolicyRepository;
import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CancellationPolicyServiceImpl implements CancellationPolicyService {

    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public List<CancellationPolicyDto> getPoliciesByBranch(Long branchId) {
        List<CancellationPolicy> list = cancellationPolicyRepository.findByBranchId(branchId);
        if (list.isEmpty()) {
            Branch branch = branchRepository.findById(branchId).orElse(null);
            if (branch != null) {
                CancellationPolicy p1 = CancellationPolicy.builder()
                        .branch(branch)
                        .hoursBefore(24)
                        .refundPercentage(new BigDecimal("100.00"))
                        .build();
                CancellationPolicy p2 = CancellationPolicy.builder()
                        .branch(branch)
                        .hoursBefore(12)
                        .refundPercentage(new BigDecimal("50.00"))
                        .build();
                cancellationPolicyRepository.save(p1);
                cancellationPolicyRepository.save(p2);
                list = cancellationPolicyRepository.findByBranchId(branchId);
            }
        }
        return list.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CancellationPolicyDto createPolicy(CancellationPolicyDto dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getBranchId()));

        CancellationPolicy policy = CancellationPolicy.builder()
                .branch(branch)
                .hoursBefore(dto.getHoursBefore() != null ? dto.getHoursBefore() : 24)
                .refundPercentage(dto.getRefundPercentage() != null ? dto.getRefundPercentage() : BigDecimal.ZERO)
                .build();

        CancellationPolicy saved = cancellationPolicyRepository.save(policy);
        return mapToDto(saved);
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

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc hủy với ID: " + id));
        cancellationPolicyRepository.delete(policy);
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
