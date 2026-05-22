package com.aifinance.financialcompanion.report.projection;

import java.math.BigDecimal;

public interface WeeklyExpenseProjection {

    String getWeekLabel();
    BigDecimal getTotalExpense();
}
