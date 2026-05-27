package com.aifinance.financialcompanion.report.projection;

import java.math.BigDecimal;

public interface CategoryGrowthProjection {
    String getCategoryName();

    BigDecimal getCurrentMonthAmount();

    BigDecimal getPreviousMonthAmount();
}
