package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record CategoryPeriodHighlightResponse(
        String period,
        String label,
        String categoryName,
        BigDecimal totalAmount
) {
}
