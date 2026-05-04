package com.aifinance.financialcompanion.admin.dto;

public record AdminUserSummaryResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
