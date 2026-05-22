package com.aifinance.financialcompanion.report.projection;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {

    String getCategoryName();
    BigDecimal getTotalAmount();

}
