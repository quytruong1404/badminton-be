package com.quy.badmintonbe.product.dto;

import com.quy.badmintonbe.common.enums.ChargeType;
import com.quy.badmintonbe.common.enums.ProductStatus;
import com.quy.badmintonbe.common.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private ProductType productType;
    private String unit;
    private ChargeType chargeType;
    private BigDecimal price;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
