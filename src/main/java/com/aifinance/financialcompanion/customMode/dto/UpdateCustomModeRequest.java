package com.aifinance.financialcompanion.customMode.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;

public record UpdateCustomModeRequest(
        String modeName,

        NotificationMode notificationMode
) {
}
