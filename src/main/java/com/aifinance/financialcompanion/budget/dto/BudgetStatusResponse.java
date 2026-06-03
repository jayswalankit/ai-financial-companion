package com.aifinance.financialcompanion.budget.dto;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        boolean budgetExist,

       BigDecimal lastBudgetAmount,

        Integer lastBudgetMonth,

        Integer  lastBudgetYear

) {
}
