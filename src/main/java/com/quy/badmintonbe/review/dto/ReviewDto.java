package com.quy.badmintonbe.review.dto;

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
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long courtId;
    private Long bookingDetailId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
