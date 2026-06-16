package com.aifinance.financialcompanion.customMode.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;

public record ActiveCustomModeResponse(
        Long modeId,
        String modeName,
        NotificationMode notificationMode
) {
}
