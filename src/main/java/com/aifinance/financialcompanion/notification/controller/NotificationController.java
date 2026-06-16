package com.aifinance.financialcompanion.notification.controller;

import com.aifinance.financialcompanion.notification.dto.DailySummaryResponse;
import com.aifinance.financialcompanion.notification.dto.MonthlySummaryResponse;
import com.aifinance.financialcompanion.notification.dto.NotificationResponse;
import com.aifinance.financialcompanion.notification.service.NotificationService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<NotificationResponse> response =
                notificationService.getNotifications(currentUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        MonthlySummaryResponse response =
                notificationService.generateMonthlySummaryResponse(currentUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(
                notificationService.generateDailySummaryResponse(currentUser)
        );
    }
}
