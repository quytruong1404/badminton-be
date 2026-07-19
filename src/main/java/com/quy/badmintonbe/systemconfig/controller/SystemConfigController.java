package com.quy.badmintonbe.systemconfig.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.systemconfig.dto.SystemConfigDto;
import com.quy.badmintonbe.systemconfig.service.SystemConfigService;
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
@RequestMapping("/api/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemConfigDto>> getConfigById(@PathVariable Long id) {
        SystemConfigDto config = systemConfigService.getConfigById(id);
        ApiResponse<SystemConfigDto> response = ApiResponse.<SystemConfigDto>builder()
                .success(true)
                .message("System config retrieved successfully")
                .data(config)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/key")
    public ResponseEntity<ApiResponse<SystemConfigDto>> getConfigByKey(@RequestParam String key) {
        SystemConfigDto config = systemConfigService.getConfigByKey(key);
        ApiResponse<SystemConfigDto> response = ApiResponse.<SystemConfigDto>builder()
                .success(true)
                .message("System config retrieved successfully")
                .data(config)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfigDto>>> getAllConfigs() {
        List<SystemConfigDto> configs = systemConfigService.getAllConfigs();
        ApiResponse<List<SystemConfigDto>> response = ApiResponse.<List<SystemConfigDto>>builder()
                .success(true)
                .message("System configs retrieved successfully")
                .data(configs)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SystemConfigDto>> createConfig(@RequestBody SystemConfigDto configDto) {
        SystemConfigDto createdConfig = systemConfigService.createConfig(configDto);
        ApiResponse<SystemConfigDto> response = ApiResponse.<SystemConfigDto>builder()
                .success(true)
                .message("System config created successfully")
                .data(createdConfig)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemConfigDto>> updateConfig(
            @PathVariable Long id, @RequestBody SystemConfigDto configDto) {
        SystemConfigDto updatedConfig = systemConfigService.updateConfig(id, configDto);
        ApiResponse<SystemConfigDto> response = ApiResponse.<SystemConfigDto>builder()
                .success(true)
                .message("System config updated successfully")
                .data(updatedConfig)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        systemConfigService.deleteConfig(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("System config deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
