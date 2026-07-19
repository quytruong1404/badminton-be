package com.quy.badmintonbe.subscription.controller;

import com.quy.badmintonbe.common.response.ApiResponse;
import com.quy.badmintonbe.subscription.dto.SubscriptionDto;
import com.quy.badmintonbe.subscription.service.SubscriptionService;
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
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getSubscriptionById(@PathVariable Long id) {
        SubscriptionDto sub = subscriptionService.getSubscriptionById(id);
        ApiResponse<SubscriptionDto> response = ApiResponse.<SubscriptionDto>builder()
                .success(true)
                .message("Subscription retrieved successfully")
                .data(sub)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getSubscriptionByCode(@PathVariable String code) {
        SubscriptionDto sub = subscriptionService.getSubscriptionByCode(code);
        ApiResponse<SubscriptionDto> response = ApiResponse.<SubscriptionDto>builder()
                .success(true)
                .message("Subscription retrieved successfully")
                .data(sub)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getSubscriptions(
            @RequestParam(required = false) Long userId) {
        List<SubscriptionDto> subs;
        if (userId != null) {
            subs = subscriptionService.getSubscriptionsByUserId(userId);
        } else {
            subs = subscriptionService.getAllSubscriptions();
        }
        ApiResponse<List<SubscriptionDto>> response = ApiResponse.<List<SubscriptionDto>>builder()
                .success(true)
                .message("Subscriptions retrieved successfully")
                .data(subs)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionDto>> createSubscription(
            @RequestBody SubscriptionDto subscriptionDto) {
        SubscriptionDto createdSub = subscriptionService.createSubscription(subscriptionDto);
        ApiResponse<SubscriptionDto> response = ApiResponse.<SubscriptionDto>builder()
                .success(true)
                .message("Subscription created successfully")
                .data(createdSub)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionDto>> updateSubscription(
            @PathVariable Long id, @RequestBody SubscriptionDto subscriptionDto) {
        SubscriptionDto updatedSub = subscriptionService.updateSubscription(id, subscriptionDto);
        ApiResponse<SubscriptionDto> response = ApiResponse.<SubscriptionDto>builder()
                .success(true)
                .message("Subscription updated successfully")
                .data(updatedSub)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        subscriptionService.cancelSubscription(id, reason);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Subscription cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
