package com.aifinance.financialcompanion.admin.dto;

import com.aifinance.financialcompanion.enums.Role;

public record CreateAdminResponse(
        String message,
        Long id,
        String username,
        String email,
        String role
) {
}
