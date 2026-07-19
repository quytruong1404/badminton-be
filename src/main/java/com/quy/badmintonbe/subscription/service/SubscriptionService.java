package com.quy.badmintonbe.subscription.service;

import com.quy.badmintonbe.subscription.dto.SubscriptionDto;
import java.util.List;

public interface SubscriptionService {
    SubscriptionDto getSubscriptionById(Long id);
    SubscriptionDto getSubscriptionByCode(String code);
    List<SubscriptionDto> getSubscriptionsByUserId(Long userId);
    List<SubscriptionDto> getAllSubscriptions();
    SubscriptionDto createSubscription(SubscriptionDto subscriptionDto);
    SubscriptionDto updateSubscription(Long id, SubscriptionDto subscriptionDto);
    void cancelSubscription(Long id, String reason);
}
