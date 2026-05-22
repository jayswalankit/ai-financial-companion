package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
       BigDecimal totalExpense,
       Long totalTransactions,
       BigDecimal averageExpense,
       BigDecimal currentMonthExpense,
       BigDecimal previousMonthExpense,
       BigDecimal monthOverMonthDifference,
       BigDecimal thisYearExpense

) {
}
