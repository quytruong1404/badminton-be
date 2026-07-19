package com.quy.badmintonbe.court.service;

import com.quy.badmintonbe.branch.entity.Branch;
import com.quy.badmintonbe.branch.repository.BranchRepository;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.common.exception.BadRequestException;
import com.quy.badmintonbe.common.enums.SlotStatus;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.subscription.repository.SubscriptionScheduleRepository;
import com.quy.badmintonbe.court.dto.CourtDto;
import com.quy.badmintonbe.court.entity.Court;
import com.quy.badmintonbe.court.entity.CourtImage;
import com.quy.badmintonbe.court.repository.CourtImageRepository;
import com.quy.badmintonbe.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final BranchRepository branchRepository;
    private final CourtImageRepository courtImageRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SubscriptionScheduleRepository subscriptionScheduleRepository;

    @Override
    public CourtDto getCourtById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + id));
        return mapToDto(court);
    }

    @Override
    public List<CourtDto> getCourtsByBranchId(Long branchId) {
        return courtRepository.findByBranchId(branchId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtDto> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CourtDto createCourt(CourtDto courtDto) {
        Court court = mapToEntity(courtDto);
        Court savedCourt = courtRepository.save(court);
        
        if (courtDto.getImageUrls() != null && !courtDto.getImageUrls().isEmpty()) {
            for (String url : courtDto.getImageUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    CourtImage img = CourtImage.builder()
                            .court(savedCourt)
                            .imageUrl(url.trim())
                            .isPrimary(true)
                            .build();
                    courtImageRepository.save(img);
                }
            }
        }
        return mapToDto(savedCourt);
    }

    @Override
    public CourtDto updateCourt(Long id, CourtDto courtDto) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + id));

        court.setName(courtDto.getName());
        court.setDescription(courtDto.getDescription());
        if (courtDto.getStatus() != null) {
            court.setStatus(courtDto.getStatus());
        }

        if (courtDto.getBranchId() != null && !court.getBranch().getId().equals(courtDto.getBranchId())) {
            Branch branch = branchRepository.findById(courtDto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + courtDto.getBranchId()));
            court.setBranch(branch);
        }

        Court updatedCourt = courtRepository.save(court);

        if (courtDto.getImageUrls() != null) {
            List<CourtImage> oldImages = courtImageRepository.findByCourtId(id);
            courtImageRepository.deleteAll(oldImages);
            for (String url : courtDto.getImageUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    CourtImage img = CourtImage.builder()
                            .court(updatedCourt)
                            .imageUrl(url.trim())
                            .isPrimary(true)
                            .build();
                    courtImageRepository.save(img);
                }
            }
        }
        return mapToDto(updatedCourt);
    }

    @Override
    public void deleteCourt(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân đấu với ID: " + id));

        // 1. Check if court has any booking details (one-time bookings)
        if (bookingDetailRepository.existsByCourtId(id)) {
            throw new BadRequestException("Không thể xóa sân đấu này vì đã có lịch đặt lẻ của khách hàng trong hệ thống.");
        }

        // 2. Check if court has active subscription schedules (recurring bookings)
        if (subscriptionScheduleRepository.existsByCourtIdAndStatus(id, SlotStatus.ACTIVE)) {
            throw new BadRequestException("Không thể xóa sân đấu này vì đang được sử dụng trong lịch đặt cố định hàng tuần đang hoạt động.");
        }

        courtRepository.delete(court);
    }

    private CourtDto mapToDto(Court court) {
        List<String> imageUrls = courtImageRepository.findByCourtId(court.getId()).stream()
                .map(CourtImage::getImageUrl)
                .collect(Collectors.toList());

        return CourtDto.builder()
                .id(court.getId())
                .branchId(court.getBranch().getId())
                .name(court.getName())
                .description(court.getDescription())
                .status(court.getStatus())
                .imageUrls(imageUrls)
                .createdAt(court.getCreatedAt())
                .updatedAt(court.getUpdatedAt())
                .build();
    }

    private Court mapToEntity(CourtDto dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + dto.getBranchId()));

        return Court.builder()
                .id(dto.getId())
                .branch(branch)
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
    }
}
