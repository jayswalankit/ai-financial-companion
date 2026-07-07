package com.aifinance.financialcompanion.report.dto;


import java.math.BigDecimal;

public record WeeklyTrendResponse(
        String weekLabel,
        BigDecimal totalExpense
) {
}
