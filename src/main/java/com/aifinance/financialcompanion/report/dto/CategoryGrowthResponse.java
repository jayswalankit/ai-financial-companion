package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record CategoryGrowthResponse(
        String categoryName,
        BigDecimal currentMonthAmount,
        BigDecimal previousMonthAmount,
        BigDecimal growthPercentage,
        String trend
) {
}
