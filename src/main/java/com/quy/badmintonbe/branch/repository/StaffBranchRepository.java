package com.quy.badmintonbe.branch.repository;

import com.quy.badmintonbe.branch.entity.StaffBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffBranchRepository extends JpaRepository<StaffBranch, Long> {
    List<StaffBranch> findByBranchId(Long branchId);
    List<StaffBranch> findByUserId(Long userId);
}
