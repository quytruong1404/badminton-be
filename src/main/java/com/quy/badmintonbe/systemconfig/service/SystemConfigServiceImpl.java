package com.quy.badmintonbe.systemconfig.service;

import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.systemconfig.dto.SystemConfigDto;
import com.quy.badmintonbe.systemconfig.entity.SystemConfig;
import com.quy.badmintonbe.systemconfig.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public SystemConfigDto getConfigById(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình hệ thống với ID: " + id));
        return mapToDto(config);
    }

    @Override
    public SystemConfigDto getConfigByKey(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình hệ thống với khóa: " + configKey));
        return mapToDto(config);
    }

    @Override
    public List<SystemConfigDto> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigDto createConfig(SystemConfigDto dto) {
        SystemConfig config = mapToEntity(dto);
        SystemConfig savedConfig = systemConfigRepository.save(config);
        return mapToDto(savedConfig);
    }

    @Override
    public SystemConfigDto updateConfig(Long id, SystemConfigDto dto) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình hệ thống với ID: " + id));

        config.setConfigValue(dto.getConfigValue());
        config.setDescription(dto.getDescription());

        SystemConfig updatedConfig = systemConfigRepository.save(config);
        return mapToDto(updatedConfig);
    }

    @Override
    public void deleteConfig(Long id) {
        SystemConfig config = systemConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình hệ thống với ID: " + id));
        systemConfigRepository.delete(config);
    }

    private SystemConfigDto mapToDto(SystemConfig config) {
        return SystemConfigDto.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private SystemConfig mapToEntity(SystemConfigDto dto) {
        return SystemConfig.builder()
                .id(dto.getId())
                .configKey(dto.getConfigKey())
                .configValue(dto.getConfigValue())
                .description(dto.getDescription())
                .build();
    }
}
