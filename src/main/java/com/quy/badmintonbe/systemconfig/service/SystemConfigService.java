package com.quy.badmintonbe.systemconfig.service;

import com.quy.badmintonbe.systemconfig.dto.SystemConfigDto;
import java.util.List;

public interface SystemConfigService {
    SystemConfigDto getConfigById(Long id);
    SystemConfigDto getConfigByKey(String configKey);
    List<SystemConfigDto> getAllConfigs();
    SystemConfigDto createConfig(SystemConfigDto configDto);
    SystemConfigDto updateConfig(Long id, SystemConfigDto configDto);
    void deleteConfig(Long id);
}
