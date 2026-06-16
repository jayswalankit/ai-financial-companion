package com.aifinance.financialcompanion.customMode.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;

public record CustomModeResponse(
        Long id,
        String modeName,
        NotificationMode notificationMode
) {
}
