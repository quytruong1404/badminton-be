package com.quy.badmintonbe.product.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.product.dto.BranchInventoryDto;
import com.quy.badmintonbe.product.service.BranchInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branch-inventories")
@RequiredArgsConstructor
public class BranchInventoryController {

    private final BranchInventoryService branchInventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchInventoryDto>>> getAllInventories(
            @RequestParam(required = false) Long branchId) {
        List<BranchInventoryDto> inventories = branchInventoryService.getAllInventories(branchId);
        ApiResponse<List<BranchInventoryDto>> response = ApiResponse.<List<BranchInventoryDto>>builder()
                .success(true)
                .message("Lấy danh sách tồn kho chi nhánh thành công")
                .data(inventories)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<BranchInventoryDto>>> getInventoriesByBranch(
            @PathVariable Long branchId) {
        List<BranchInventoryDto> inventories = branchInventoryService.getInventoriesByBranchId(branchId);
        ApiResponse<List<BranchInventoryDto>> response = ApiResponse.<List<BranchInventoryDto>>builder()
                .success(true)
                .message("Lấy danh sách tồn kho cho chi nhánh thành công")
                .data(inventories)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchInventoryDto>> getInventoryById(@PathVariable Long id) {
        BranchInventoryDto inventory = branchInventoryService.getInventoryById(id);
        ApiResponse<BranchInventoryDto> response = ApiResponse.<BranchInventoryDto>builder()
                .success(true)
                .message("Lấy thông tin tồn kho thành công")
                .data(inventory)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchInventoryDto>> addBranchInventory(@RequestBody Map<String, Object> payload) {
        Long branchId = payload.get("branchId") != null ? ((Number) payload.get("branchId")).longValue() : null;
        Long productId = payload.get("productId") != null ? ((Number) payload.get("productId")).longValue() : null;
        Integer quantity = payload.get("quantity") != null ? ((Number) payload.get("quantity")).intValue() : 50;
        Integer lowStockThreshold = payload.get("lowStockThreshold") != null ? ((Number) payload.get("lowStockThreshold")).intValue() : 5;

        BranchInventoryDto created = branchInventoryService.addBranchInventory(branchId, productId, quantity, lowStockThreshold);
        ApiResponse<BranchInventoryDto> response = ApiResponse.<BranchInventoryDto>builder()
                .success(true)
                .message("Thêm sản phẩm vào kho chi nhánh thành công")
                .data(created)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchInventoryDto>> updateInventory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Integer quantity = payload.get("quantity") != null ? ((Number) payload.get("quantity")).intValue() : null;
        Integer lowStockThreshold = payload.get("lowStockThreshold") != null ? ((Number) payload.get("lowStockThreshold")).intValue() : null;

        BranchInventoryDto updated = branchInventoryService.updateInventory(id, quantity, lowStockThreshold);
        ApiResponse<BranchInventoryDto> response = ApiResponse.<BranchInventoryDto>builder()
                .success(true)
                .message("Cập nhật tồn kho chi nhánh thành công")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBranchInventory(@PathVariable Long id) {
        branchInventoryService.deleteBranchInventory(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa sản phẩm khỏi kho chi nhánh thành công")
                .build();
        return ResponseEntity.ok(response);
    }
}
