package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingPatternResponse(
        String allTimeHighestSpendingCategory,
        BigDecimal allTimeHighestSpendingAmount,
        String currentMonthHighestCategory,
        BigDecimal currentMonthHighestCategoryAmount,
        BigDecimal averageDailyExpenses,
        BigDecimal highestSingleExpense,
        LocalDate highestExpenseDate
) {
}
