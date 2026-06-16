package com.aifinance.financialcompanion.notification.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        BigDecimal totalSpent,
        BigDecimal remainingBudget,
        String budgetStatus,
        String topCategory
) {
}
