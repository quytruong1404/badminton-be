package com.quy.badmintonbe.subscription.repository;

import com.quy.badmintonbe.subscription.entity.SubscriptionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.quy.badmintonbe.common.enums.SlotStatus;

@Repository
public interface SubscriptionScheduleRepository extends JpaRepository<SubscriptionSchedule, Long> {
    List<SubscriptionSchedule> findBySubscriptionId(Long subscriptionId);
    boolean existsBySlotIdAndStatus(Long slotId, SlotStatus status);
    boolean existsByCourtIdAndStatus(Long courtId, SlotStatus status);
}
