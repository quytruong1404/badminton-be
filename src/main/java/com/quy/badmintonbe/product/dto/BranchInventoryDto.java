package com.quy.badmintonbe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchInventoryDto {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long productId;
    private String productName;
    private String productType;
    private String unit;
    private String chargeType;
    private BigDecimal price;
    private String productStatus;
    private Integer quantity;
    private Integer lowStockThreshold;
    private LocalDateTime updatedAt;
}
