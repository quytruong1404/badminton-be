package com.quy.badmintonbe.branch.controller;

import com.quy.badmintonbe.branch.dto.BranchDto;
import com.quy.badmintonbe.branch.service.BranchService;
import com.quy.badmintonbe.common.response.ApiResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDto>> getBranchById(@PathVariable Long id) {
        BranchDto branch = branchService.getBranchById(id);
        ApiResponse<BranchDto> response = ApiResponse.<BranchDto>builder()
                .success(true)
                .message("Branch retrieved successfully")
                .data(branch)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDto>>> getAllBranches() {
        List<BranchDto> branches = branchService.getAllBranches();
        ApiResponse<List<BranchDto>> response = ApiResponse.<List<BranchDto>>builder()
                .success(true)
                .message("All branches retrieved successfully")
                .data(branches)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchDto>> createBranch(@RequestBody BranchDto branchDto) {
        BranchDto createdBranch = branchService.createBranch(branchDto);
        ApiResponse<BranchDto> response = ApiResponse.<BranchDto>builder()
                .success(true)
                .message("Branch created successfully")
                .data(createdBranch)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(@PathVariable Long id, @RequestBody BranchDto branchDto) {
        BranchDto updatedBranch = branchService.updateBranch(id, branchDto);
        ApiResponse<BranchDto> response = ApiResponse.<BranchDto>builder()
                .success(true)
                .message("Branch updated successfully")
                .data(updatedBranch)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Branch deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
