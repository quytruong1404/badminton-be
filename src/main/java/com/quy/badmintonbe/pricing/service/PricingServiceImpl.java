package com.quy.badmintonbe.pricing.service;

import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.entity.TimeSlot;
import com.quy.badmintonbe.court.repository.CourtRepository;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.pricing.dto.PricingRuleDto;
import com.quy.badmintonbe.pricing.dto.PricingRuleBulkRequest;
import com.quy.badmintonbe.pricing.entity.PricingRule;
import com.quy.badmintonbe.pricing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Override
    public PricingRuleDto getPricingRuleById(Long id) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc cấu hình giá với ID: " + id));
        return mapToDto(rule);
    }

    @Override
    public List<PricingRuleDto> getPricingRulesByCourtId(Long courtId) {
        return pricingRuleRepository.findByCourtId(courtId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PricingRuleDto> getAllPricingRules() {
        return pricingRuleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PricingRuleDto createPricingRule(PricingRuleDto dto) {
        PricingRule rule = mapToEntity(dto);
        PricingRule savedRule = pricingRuleRepository.save(rule);
        return mapToDto(savedRule);
    }

    @Override
    public PricingRuleDto updatePricingRule(Long id, PricingRuleDto dto) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc cấu hình giá với ID: " + id));

        rule.setPrice(dto.getPrice());
        rule.setDayType(dto.getDayType());
        if (dto.getStatus() != null) {
            rule.setStatus(dto.getStatus());
        }

        if (dto.getCourtId() != null && !rule.getCourt().getId().equals(dto.getCourtId())) {
            Court court = courtRepository.findById(dto.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + dto.getCourtId()));
            rule.setCourt(court);
        }

        if (dto.getSlotId() != null && !rule.getSlot().getId().equals(dto.getSlotId())) {
            TimeSlot slot = timeSlotRepository.findById(dto.getSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + dto.getSlotId()));
            rule.setSlot(slot);
        }

        PricingRule updatedRule = pricingRuleRepository.save(rule);
        return mapToDto(updatedRule);
    }

    @Override
    public void deletePricingRule(Long id) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quy tắc cấu hình giá với ID: " + id));
        pricingRuleRepository.delete(rule);
    }

    private PricingRuleDto mapToDto(PricingRule rule) {
        return PricingRuleDto.builder()
                .id(rule.getId())
                .courtId(rule.getCourt().getId())
                .slotId(rule.getSlot().getId())
                .dayType(rule.getDayType())
                .price(rule.getPrice())
                .status(rule.getStatus())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private PricingRule mapToEntity(PricingRuleDto dto) {
        Court court = courtRepository.findById(dto.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + dto.getCourtId()));
        TimeSlot slot = timeSlotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + dto.getSlotId()));

        return PricingRule.builder()
                .id(dto.getId())
                .court(court)
                .slot(slot)
                .dayType(dto.getDayType())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void createPricingRulesBulk(PricingRuleBulkRequest request) {
        for (Long courtId : request.getCourtIds()) {
            Court court = courtRepository.findById(courtId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + courtId));

            for (Long slotId : request.getSlotIds()) {
                TimeSlot slot = timeSlotRepository.findById(slotId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + slotId));

                for (com.quy.badmintonbe.common.enums.DayType dayType : request.getDayTypes()) {
                    java.util.Optional<PricingRule> existingOpt = pricingRuleRepository
                            .findByCourtIdAndSlotIdAndDayType(courtId, slotId, dayType);

                    PricingRule rule;
                    if (existingOpt.isPresent()) {
                        rule = existingOpt.get();
                        rule.setPrice(request.getPrice());
                        if (request.getStatus() != null) {
                            rule.setStatus(request.getStatus());
                        }
                    } else {
                        rule = PricingRule.builder()
                                .court(court)
                                .slot(slot)
                                .dayType(dayType)
                                .price(request.getPrice())
                                .status(request.getStatus() != null ? request.getStatus() : com.quy.badmintonbe.common.enums.SlotStatus.ACTIVE)
                                .build();
                    }
                    pricingRuleRepository.save(rule);
                }
            }
        }
    }
}
