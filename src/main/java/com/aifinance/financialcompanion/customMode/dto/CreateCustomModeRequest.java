package com.aifinance.financialcompanion.customMode.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;

public record CreateCustomModeRequest(
        String modeName,
        NotificationMode notificationMode
) {
}
