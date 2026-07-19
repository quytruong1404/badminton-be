package com.quy.badmintonbe.product.repository;

import com.quy.badmintonbe.product.entity.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
    List<BranchInventory> findByBranchId(Long branchId);
    Optional<BranchInventory> findByBranchIdAndProductId(Long branchId, Long productId);
}
