package com.quy.badmintonbe.court.service;

import com.quy.badmintonbe.court.dto.CourtDto;
import java.util.List;

public interface CourtService {
    CourtDto getCourtById(Long id);
    List<CourtDto> getCourtsByBranchId(Long branchId);
    List<CourtDto> getAllCourts();
    CourtDto createCourt(CourtDto courtDto);
    CourtDto updateCourt(Long id, CourtDto courtDto);
    void deleteCourt(Long id);
}
