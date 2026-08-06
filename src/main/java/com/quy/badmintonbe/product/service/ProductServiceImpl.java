package com.quy.badmintonbe.product.service;

import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.product.dto.ProductDto;
import com.quy.badmintonbe.product.entity.Product;
import com.quy.badmintonbe.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.product.entity.BranchInventory;
import com.quy.badmintonbe.product.repository.BranchInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final BranchInventoryRepository branchInventoryRepository;

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ/sản phẩm với ID: " + id));
        return mapToDto(product);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = mapToEntity(dto);
        Product savedProduct = productRepository.save(product);

        branchRepository.findAll().forEach(branch -> {
            BranchInventory inv = BranchInventory.builder()
                    .branch(branch)
                    .product(savedProduct)
                    .quantity(50)
                    .lowStockThreshold(5)
                    .build();
            branchInventoryRepository.save(inv);
        });

        return mapToDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ/sản phẩm với ID: " + id));

        product.setName(dto.getName());
        product.setProductType(dto.getProductType());
        product.setUnit(dto.getUnit());
        product.setChargeType(dto.getChargeType());
        product.setPrice(dto.getPrice());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ/sản phẩm với ID: " + id));
        productRepository.delete(product);
    }

    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .productType(product.getProductType())
                .unit(product.getUnit())
                .chargeType(product.getChargeType())
                .price(product.getPrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product mapToEntity(ProductDto dto) {
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .productType(dto.getProductType())
                .unit(dto.getUnit())
                .chargeType(dto.getChargeType())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .build();
    }
}
