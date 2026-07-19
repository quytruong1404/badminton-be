package com.quy.badmintonbe.subscription.repository;

import com.quy.badmintonbe.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriptionCode(String subscriptionCode);
    List<Subscription> findByUserId(Long userId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
