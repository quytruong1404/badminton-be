package com.quy.badmintonbe.product.service;

import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.common.exception.BadRequestException;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.product.dto.BranchInventoryDto;
import com.quy.badmintonbe.product.entity.BranchInventory;
import com.quy.badmintonbe.product.entity.Product;
import com.quy.badmintonbe.product.repository.BranchInventoryRepository;
import com.quy.badmintonbe.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchInventoryServiceImpl implements BranchInventoryService {

    private final BranchInventoryRepository branchInventoryRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    @Override
    public List<BranchInventoryDto> getAllInventories(Long branchId) {
        if (branchId != null) {
            return getInventoriesByBranchId(branchId);
        }
        return branchInventoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BranchInventoryDto> getInventoriesByBranchId(Long branchId) {
        return branchInventoryRepository.findByBranchId(branchId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BranchInventoryDto getInventoryById(Long id) {
        BranchInventory inventory = branchInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho với ID: " + id));
        return mapToDto(inventory);
    }

    @Override
    @Transactional
    public BranchInventoryDto addBranchInventory(Long branchId, Long productId, Integer quantity, Integer lowStockThreshold) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + branchId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm/dịch vụ với ID: " + productId));

        List<BranchInventory> existingList = branchInventoryRepository.findAllByBranchIdAndProductId(branchId, productId);
        if (!existingList.isEmpty()) {
            throw new BadRequestException("Sản phẩm [" + product.getName() + "] đã có trong kho của chi nhánh [" + branch.getName() + "].");
        }

        int qty = (quantity != null && quantity >= 0) ? quantity : 50;
        int threshold = (lowStockThreshold != null && lowStockThreshold >= 0) ? lowStockThreshold : 5;

        BranchInventory newInv = BranchInventory.builder()
                .branch(branch)
                .product(product)
                .quantity(qty)
                .lowStockThreshold(threshold)
                .build();

        BranchInventory saved = branchInventoryRepository.save(newInv);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public BranchInventoryDto updateInventory(Long id, Integer quantity, Integer lowStockThreshold) {
        BranchInventory inventory = branchInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho với ID: " + id));

        if (quantity != null) {
            if (quantity < 0) {
                throw new BadRequestException("Số lượng tồn kho không được âm.");
            }
            inventory.setQuantity(quantity);
        }

        if (lowStockThreshold != null) {
            if (lowStockThreshold < 0) {
                throw new BadRequestException("Ngưỡng cảnh báo sắp hết hàng không được âm.");
            }
            inventory.setLowStockThreshold(lowStockThreshold);
        }

        BranchInventory saved = branchInventoryRepository.save(inventory);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteBranchInventory(Long id) {
        BranchInventory inventory = branchInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho với ID: " + id));
        branchInventoryRepository.delete(inventory);
    }

    @Override
    @Transactional
    public void deductStock(Long branchId, Long productId, Integer quantityToDeduct) {
        if (quantityToDeduct == null || quantityToDeduct <= 0) {
            return;
        }

        List<BranchInventory> inventories = branchInventoryRepository.findAllByBranchIdAndProductId(branchId, productId);
        if (inventories.isEmpty()) {
            throw new BadRequestException("Sản phẩm chưa được khởi tạo kho tại chi nhánh này.");
        }

        BranchInventory inventory = inventories.get(0);
        if (inventories.size() > 1) {
            for (int i = 1; i < inventories.size(); i++) {
                try {
                    branchInventoryRepository.delete(inventories.get(i));
                } catch (Exception ignored) {}
            }
        }

        int currentStock = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        if (currentStock < quantityToDeduct) {
            throw new BadRequestException("Sản phẩm [" + inventory.getProduct().getName() + "] tại chi nhánh [" 
                    + inventory.getBranch().getName() + "] chỉ còn " + currentStock + " " + inventory.getProduct().getUnit() 
                    + ", không đủ số lượng đặt (" + quantityToDeduct + ").");
        }

        inventory.setQuantity(currentStock - quantityToDeduct);
        branchInventoryRepository.save(inventory);
    }

    private BranchInventoryDto mapToDto(BranchInventory inv) {
        return BranchInventoryDto.builder()
                .id(inv.getId())
                .branchId(inv.getBranch().getId())
                .branchName(inv.getBranch().getName())
                .productId(inv.getProduct().getId())
                .productName(inv.getProduct().getName())
                .productType(inv.getProduct().getProductType() != null ? inv.getProduct().getProductType().name() : "SELL")
                .unit(inv.getProduct().getUnit())
                .chargeType(inv.getProduct().getChargeType() != null ? inv.getProduct().getChargeType().name() : "PER_UNIT")
                .price(inv.getProduct().getPrice())
                .productStatus(inv.getProduct().getStatus() != null ? inv.getProduct().getStatus().name() : "ACTIVE")
                .quantity(inv.getQuantity())
                .lowStockThreshold(inv.getLowStockThreshold())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
