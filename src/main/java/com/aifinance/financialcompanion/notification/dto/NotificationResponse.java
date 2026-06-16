package com.aifinance.financialcompanion.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String message,

        String severity,

        LocalDateTime sentAt
) {
}
