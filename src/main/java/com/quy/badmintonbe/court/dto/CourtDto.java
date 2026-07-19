package com.quy.badmintonbe.court.dto;

import com.quy.badmintonbe.common.enums.CourtStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtDto {
    private Long id;
    private Long branchId;
    private String name;
    private String description;
    private CourtStatus status;
    private java.util.List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
