package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record FinancialHealthResponse(
        BigDecimal totalSpent,
        BigDecimal monthlyBudget,
        BigDecimal remainingBudget,
        BigDecimal budgetUsagePercentage,
        String financialStatus
) {
}
