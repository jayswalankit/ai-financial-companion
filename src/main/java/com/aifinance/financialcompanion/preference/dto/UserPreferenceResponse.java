package com.aifinance.financialcompanion.preference.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;

public record UserPreferenceResponse(
        UserMode userMode,
        NotificationMode notificationMode
) {
}
