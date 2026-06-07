package com.aifinance.financialcompanion.preference.dto;

import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;
import jakarta.validation.constraints.NotNull;

public record UpdateUserPreferenceRequest(
        @NotNull
        UserMode userMode,

        @NotNull
        NotificationMode notificationMode
) {
}
