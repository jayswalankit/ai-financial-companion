package com.aifinance.financialcompanion.report.controller;


import com.aifinance.financialcompanion.notification.service.NotificationService;
import com.aifinance.financialcompanion.report.dto.*;
import com.aifinance.financialcompanion.report.service.ReportService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getDashboardSummary(currentUser));
    }

    @GetMapping("/top-categories")
    public ResponseEntity<List<CategorySummaryResponse>> getTopCategories(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getTopCategories(currentUser));
    }

    @GetMapping("/budget-status")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getBudgetStatus(currentUser));
    }

    @GetMapping("/insights")
    public ResponseEntity<List<InsightResponse>> getInsights(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<InsightResponse> insights =
                reportService.generateBasicInsights(currentUser);

        notificationService.createNotificationsFromInsights(
                currentUser,
                insights
        );

        return ResponseEntity.ok(insights);
    }

    @GetMapping("/weekly-trend")
    public ResponseEntity<List<WeeklyTrendResponse>> getWeeklyTrend(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getWeeklyTrend(currentUser));
    }

    @GetMapping("/monthlyComparison")
    public ResponseEntity<MonthlyComparisonResponse>getMonthlyComparison(@AuthenticationPrincipal CustomUserDetails currentUser){
        return ResponseEntity.ok(reportService.getMonthlyComparison(currentUser));
    }

    @GetMapping("/financialHealth")
    public ResponseEntity<FinancialHealthResponse> getFinancialHealth(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getFinancialHealth(currentUser));
    }

    @GetMapping("/spendingPatterns")
    public ResponseEntity<SpendingPatternResponse> getSpendingPatterns(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getSpendingPattern(currentUser));
    }

    @GetMapping("/categoryGrowth")
    public ResponseEntity<List<CategoryGrowthResponse>> getCategoryGrowthAnalysis(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reportService.getCategoryGrowth(currentUser));
    }
}
