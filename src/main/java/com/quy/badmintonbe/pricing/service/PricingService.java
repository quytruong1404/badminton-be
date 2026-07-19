package com.quy.badmintonbe.pricing.service;

import com.quy.badmintonbe.pricing.dto.PricingRuleDto;
import com.quy.badmintonbe.pricing.dto.PricingRuleBulkRequest;
import java.util.List;

public interface PricingService {
    PricingRuleDto getPricingRuleById(Long id);
    List<PricingRuleDto> getPricingRulesByCourtId(Long courtId);
    List<PricingRuleDto> getAllPricingRules();
    PricingRuleDto createPricingRule(PricingRuleDto pricingRuleDto);
    PricingRuleDto updatePricingRule(Long id, PricingRuleDto pricingRuleDto);
    void deletePricingRule(Long id);
    void createPricingRulesBulk(PricingRuleBulkRequest request);
}
