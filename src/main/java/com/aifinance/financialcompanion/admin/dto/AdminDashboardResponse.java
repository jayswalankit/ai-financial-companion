package com.aifinance.financialcompanion.admin.dto;

import java.util.List;

public record AdminDashboardResponse(
        String message,
        List<AdminUserSummaryResponse> users
) {
}
