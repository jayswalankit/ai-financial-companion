package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        String categoryName,
        BigDecimal totalAmount
) {
}
