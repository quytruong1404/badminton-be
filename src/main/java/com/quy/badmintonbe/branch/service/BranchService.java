package com.quy.badmintonbe.branch.service;

import com.quy.badmintonbe.branch.dto.BranchDto;
import java.util.List;

public interface BranchService {
    BranchDto getBranchById(Long id);
    List<BranchDto> getAllBranches();
    BranchDto createBranch(BranchDto branchDto);
    BranchDto updateBranch(Long id, BranchDto branchDto);
    void deleteBranch(Long id);
}
