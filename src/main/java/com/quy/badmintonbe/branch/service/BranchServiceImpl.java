package com.quy.badmintonbe.branch.service;

import com.quy.badmintonbe.branch.dto.BranchDto;
import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    public BranchDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        return mapToDto(branch);
    }

    @Override
    public List<BranchDto> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDto createBranch(BranchDto branchDto) {
        Branch branch = mapToEntity(branchDto);
        Branch savedBranch = branchRepository.save(branch);
        return mapToDto(savedBranch);
    }

    @Override
    public BranchDto updateBranch(Long id, BranchDto branchDto) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        branch.setName(branchDto.getName());
        branch.setAddress(branchDto.getAddress());
        branch.setPhoneNumber(branchDto.getPhoneNumber());
        branch.setOpenTime(branchDto.getOpenTime());
        branch.setCloseTime(branchDto.getCloseTime());
        if (branchDto.getStatus() != null) {
            branch.setStatus(branchDto.getStatus());
        }

        Branch updatedBranch = branchRepository.save(branch);
        return mapToDto(updatedBranch);
    }

    @Override
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        branchRepository.delete(branch);
    }

    private BranchDto mapToDto(Branch branch) {
        return BranchDto.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phoneNumber(branch.getPhoneNumber())
                .openTime(branch.getOpenTime())
                .closeTime(branch.getCloseTime())
                .status(branch.getStatus())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    private Branch mapToEntity(BranchDto dto) {
        return Branch.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .phoneNumber(dto.getPhoneNumber())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .status(dto.getStatus())
                .build();
    }
}
