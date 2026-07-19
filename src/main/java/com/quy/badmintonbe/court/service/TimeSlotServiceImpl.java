package com.quy.badmintonbe.court.service;

import com.quy.badmintonbe.common.exception.BadRequestException;
import com.quy.badmintonbe.common.exception.ResourceNotFoundException;
import com.quy.badmintonbe.common.enums.SlotStatus;
import com.quy.badmintonbe.booking.repository.BookingDetailRepository;
import com.quy.badmintonbe.subscription.repository.SubscriptionScheduleRepository;
import com.quy.badmintonbe.court.dto.TimeSlotDto;
import com.quy.badmintonbe.court.entity.TimeSlot;
import com.quy.badmintonbe.court.repository.TimeSlotRepository;
import com.quy.badmintonbe.booking.repository.CourtReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SubscriptionScheduleRepository subscriptionScheduleRepository;
    private final CourtReservationRepository courtReservationRepository;

    @Override
    public TimeSlotDto getTimeSlotById(Long id) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay ca choi voi ID: " + id));
        return mapToDto(slot);
    }

    @Override
    public List<TimeSlotDto> getAllTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TimeSlotDto createTimeSlot(TimeSlotDto dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BadRequestException("Phải cung cấp đầy đủ giờ bắt đầu và giờ kết thúc.");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new BadRequestException("Giờ bắt đầu ca chơi phải trước giờ kết thúc ca chơi.");
        }
        if (!timeSlotRepository.findByStartTimeAndEndTime(dto.getStartTime(), dto.getEndTime()).isEmpty()) {
            throw new BadRequestException("Ca chơi từ " + dto.getStartTime().toString().substring(0, 5) 
                    + " đến " + dto.getEndTime().toString().substring(0, 5) + " đã tồn tại trong hệ thống.");
        }
        TimeSlot slot = mapToEntity(dto);
        TimeSlot saved = timeSlotRepository.save(slot);
        return mapToDto(saved);
    }

    @Override
    public TimeSlotDto updateTimeSlot(Long id, TimeSlotDto dto) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay ca choi voi ID: " + id));
        
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new BadRequestException("Phải cung cấp đầy đủ giờ bắt đầu và giờ kết thúc.");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new BadRequestException("Giờ bắt đầu ca chơi phải trước giờ kết thúc ca chơi.");
        }
        java.util.List<TimeSlot> existing = timeSlotRepository.findByStartTimeAndEndTime(dto.getStartTime(), dto.getEndTime());
        boolean isDuplicate = existing.stream().anyMatch(slotItem -> !slotItem.getId().equals(id));
        if (isDuplicate) {
            throw new BadRequestException("Ca chơi từ " + dto.getStartTime().toString().substring(0, 5) 
                    + " đến " + dto.getEndTime().toString().substring(0, 5) + " đã tồn tại trong hệ thống.");
        }

        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        if (dto.getStatus() != null) {
            slot.setStatus(dto.getStatus());
        }
        
        TimeSlot updated = timeSlotRepository.save(slot);
        return mapToDto(updated);
    }

    @Override
    public void deleteTimeSlot(Long id) {
        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca chơi với ID: " + id));
        
        // 1. Check if slot has any booking details (one-time bookings)
        if (bookingDetailRepository.existsBySlotId(id)) {
            throw new BadRequestException("Không thể xóa ca chơi này vì đã có lịch đặt lẻ của khách hàng trong hệ thống.");
        }

        // 2. Check if slot is actively used in any subscription schedules (recurring bookings)
        if (subscriptionScheduleRepository.existsBySlotIdAndStatus(id, SlotStatus.ACTIVE)) {
            throw new BadRequestException("Không thể xóa ca chơi này vì đang được sử dụng trong lịch đặt cố định hàng tuần đang hoạt động.");
        }

        // 3. Check if slot has any court reservations (temporary holds or details)
        if (courtReservationRepository.existsBySlotId(id)) {
            throw new BadRequestException("Không thể xóa ca chơi này vì đã có dữ liệu giữ chỗ hoặc đặt sân liên quan trong hệ thống. Bạn hãy đổi trạng thái ca chơi sang INACTIVE để ngưng hoạt động.");
        }

        timeSlotRepository.delete(slot);
    }

    private TimeSlotDto mapToDto(TimeSlot entity) {
        return TimeSlotDto.builder()
                .id(entity.getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TimeSlot mapToEntity(TimeSlotDto dto) {
        return TimeSlot.builder()
                .id(dto.getId())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(dto.getStatus() != null ? dto.getStatus() : com.quy.badmintonbe.common.enums.SlotStatus.ACTIVE)
                .build();
    }
}
