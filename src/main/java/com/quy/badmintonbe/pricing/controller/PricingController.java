package com.quy.badmintonbe.pricing.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.pricing.dto.PricingRuleDto;
import com.quy.badmintonbe.pricing.dto.PricingRuleBulkRequest;
import com.quy.badmintonbe.pricing.service.PricingService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleDto>> getPricingRuleById(@PathVariable Long id) {
        PricingRuleDto rule = pricingService.getPricingRuleById(id);
        ApiResponse<PricingRuleDto> response = ApiResponse.<PricingRuleDto>builder()
                .success(true)
                .message("Pricing rule retrieved successfully")
                .data(rule)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PricingRuleDto>>> getPricingRules(
            @RequestParam(required = false) Long courtId) {
        List<PricingRuleDto> rules;
        if (courtId != null) {
            rules = pricingService.getPricingRulesByCourtId(courtId);
        } else {
            rules = pricingService.getAllPricingRules();
        }
        ApiResponse<List<PricingRuleDto>> response = ApiResponse.<List<PricingRuleDto>>builder()
                .success(true)
                .message("Pricing rules retrieved successfully")
                .data(rules)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PricingRuleDto>> createPricingRule(
            @RequestBody PricingRuleDto pricingRuleDto) {
        PricingRuleDto createdRule = pricingService.createPricingRule(pricingRuleDto);
        ApiResponse<PricingRuleDto> response = ApiResponse.<PricingRuleDto>builder()
                .success(true)
                .message("Pricing rule created successfully")
                .data(createdRule)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingRuleDto>> updatePricingRule(
            @PathVariable Long id, @RequestBody PricingRuleDto pricingRuleDto) {
        PricingRuleDto updatedRule = pricingService.updatePricingRule(id, pricingRuleDto);
        ApiResponse<PricingRuleDto> response = ApiResponse.<PricingRuleDto>builder()
                .success(true)
                .message("Pricing rule updated successfully")
                .data(updatedRule)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePricingRule(@PathVariable Long id) {
        pricingService.deletePricingRule(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Pricing rule deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> createPricingRulesBulk(
            @RequestBody PricingRuleBulkRequest request) {
        pricingService.createPricingRulesBulk(request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Bulk pricing rules updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
