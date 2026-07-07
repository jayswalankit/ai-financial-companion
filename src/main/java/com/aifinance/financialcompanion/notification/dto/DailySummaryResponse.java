package com.aifinance.financialcompanion.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public record DailySummaryResponse(
        BigDecimal todaySpent,
       String todayTopCategory,
        long expenseCount,
        LocalDate summaryDate,
        BigDecimal recommendedDailyLimit
        ){
}
