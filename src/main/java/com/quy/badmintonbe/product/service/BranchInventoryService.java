package com.quy.badmintonbe.product.service;

import com.quy.badmintonbe.product.dto.BranchInventoryDto;

import java.util.List;

public interface BranchInventoryService {
    List<BranchInventoryDto> getAllInventories(Long branchId);
    List<BranchInventoryDto> getInventoriesByBranchId(Long branchId);
    BranchInventoryDto getInventoryById(Long id);
    BranchInventoryDto addBranchInventory(Long branchId, Long productId, Integer quantity, Integer lowStockThreshold);
    BranchInventoryDto updateInventory(Long id, Integer quantity, Integer lowStockThreshold);
    void deleteBranchInventory(Long id);
    void deductStock(Long branchId, Long productId, Integer quantityToDeduct);
}
