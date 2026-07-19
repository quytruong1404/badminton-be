package com.quy.badmintonbe.pricing.repository;

import com.quy.badmintonbe.common.enums.DayType;
import com.quy.badmintonbe.pricing.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByCourtId(Long courtId);
    Optional<PricingRule> findByCourtIdAndSlotIdAndDayType(Long courtId, Long slotId, DayType dayType);
}
