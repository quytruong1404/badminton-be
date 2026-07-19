package com.quy.badmintonbe.court.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.court.dto.CourtDto;
import com.quy.badmintonbe.court.service.CourtService;
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
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtDto>> getCourtById(@PathVariable Long id) {
        CourtDto court = courtService.getCourtById(id);
        ApiResponse<CourtDto> response = ApiResponse.<CourtDto>builder()
                .success(true)
                .message("Court retrieved successfully")
                .data(court)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourtDto>>> getCourts(
            @RequestParam(required = false) Long branchId) {
        List<CourtDto> courts;
        if (branchId != null) {
            courts = courtService.getCourtsByBranchId(branchId);
        } else {
            courts = courtService.getAllCourts();
        }
        ApiResponse<List<CourtDto>> response = ApiResponse.<List<CourtDto>>builder()
                .success(true)
                .message("Courts retrieved successfully")
                .data(courts)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourtDto>> createCourt(@RequestBody CourtDto courtDto) {
        CourtDto createdCourt = courtService.createCourt(courtDto);
        ApiResponse<CourtDto> response = ApiResponse.<CourtDto>builder()
                .success(true)
                .message("Court created successfully")
                .data(createdCourt)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtDto>> updateCourt(
            @PathVariable Long id, @RequestBody CourtDto courtDto) {
        CourtDto updatedCourt = courtService.updateCourt(id, courtDto);
        ApiResponse<CourtDto> response = ApiResponse.<CourtDto>builder()
                .success(true)
                .message("Court updated successfully")
                .data(updatedCourt)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Court deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
