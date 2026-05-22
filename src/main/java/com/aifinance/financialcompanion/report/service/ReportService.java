package com.aifinance.financialcompanion.report.service;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.report.dto.*;
import com.aifinance.financialcompanion.report.projection.CategoryExpenseProjection;
import com.aifinance.financialcompanion.report.projection.WeeklyExpenseProjection;
import com.aifinance.financialcompanion.report.repo.ReportRepository;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepo userRepo;

    private static final int MONEY_SCALE = 2;
    private static final int TOP_CATEGORIES_LIMIT = 5;
    private static final BigDecimal WARNING_THRESHOLD_PERCENT = new BigDecimal("80");

    @Transactional(readOnly = true)
   public DashboardSummaryResponse getDashboardSummary(CustomUserDetails currentUser){

    User user = getAuthenticatedUser(currentUser);
    log.info("Generating dashboard summary for userId = {}",user.getId());

    BigDecimal totalExpense = safe(reportRepository.getTotalExpenseByUser(user));

    Long totalTransaction = safeCount(reportRepository.getTotalTransactionCountByUser(user));

    BigDecimal averageExpense = calculateAverageExpense(totalExpense,totalTransaction);

    YearMonth currentMont = YearMonth.now();

    BigDecimal currentMonthExpense = getMonthExpense(user,currentMont);

    BigDecimal previousMonthExpense = getMonthExpense(user,currentMont.minusMonths(1));

    BigDecimal monthOverMonthDifference = currentMonthExpense.subtract(previousMonthExpense);


   LocalDate today = LocalDate.now();
   LocalDate startOfYear = today.withDayOfYear(1);

  BigDecimal thisYearExpense = safe(reportRepository.getTotalExpenseByUserAndDateBetween(user,startOfYear,today));

    return new DashboardSummaryResponse(totalExpense,
            totalTransaction,
            averageExpense,
            currentMonthExpense,
            previousMonthExpense,
            monthOverMonthDifference,
            thisYearExpense);
}

@Transactional(readOnly = true)
 public List<CategorySummaryResponse> getTopCategories(CustomUserDetails currentUser){
    User user = getAuthenticatedUser(currentUser);
    return getTopCategoriesForUser(user);
}

@Transactional(readOnly = true)
 public BudgetStatusResponse getBudgetStatus(CustomUserDetails currentUser, BigDecimal monthlyBudget){

        validateMonthlyBudget(monthlyBudget);

        User user = getAuthenticatedUser(currentUser);

        BigDecimal currentSpent = getMonthExpense(user,YearMonth.now());

        BigDecimal remainingBudget = monthlyBudget.subtract(currentSpent);

        int remainingDays  = getRemainingDaysInCurrentMonth();

        BigDecimal recommendedDailyLimit = remainingBudget.divide(BigDecimal.valueOf(remainingDays),MONEY_SCALE,RoundingMode.HALF_UP);

        String status = resolveSeverity(currentSpent,monthlyBudget);

        log.info("Generated budget status for userId = {}, monthlyBudget = {}, currentSpent = {}, remainigBudget = {}, remainingDays = {}",user.getId(),
                monthlyBudget,
                currentSpent,
                remainingBudget,
                remainingDays);

        return new BudgetStatusResponse(
                monthlyBudget,
                currentSpent,
                remainingBudget,
                recommendedDailyLimit,
                status
        );
}

@Transactional(readOnly = true)
public List<InsightResponse> generateBasicInsights(CustomUserDetails currentUser, BigDecimal monthlyBudget){
        validateMonthlyBudget(monthlyBudget);

        User user = getAuthenticatedUser(currentUser);
        BigDecimal currentSpent = getMonthExpense(user,YearMonth.now());

        List<InsightResponse> insights = new ArrayList<>();

        if(currentSpent.compareTo(monthlyBudget) > 0){
            insights.add(new InsightResponse("You have exceeded your monthly budget","CRITICAL"));
        } else if (isAboveWarningThreshold(currentSpent,monthlyBudget)) {
            insights.add(new InsightResponse("You are close to exceeding your monthly budget","WARNING"));
        }
        else{
            insights.add(new InsightResponse("Your spending is currently within budget","INFO"));
        }

        List<CategorySummaryResponse> topCategories = getTopCategoriesForUser(user);
        if(!topCategories.isEmpty()){
            CategorySummaryResponse topCategory = topCategories.get(0);
            insights.add(new InsightResponse(
                    topCategory.categoryName() + "is one of your highest expense categories","INFO"
            ));
        }

        log.info("Generated {} basic insights for userId = {}", insights.size(),user.getId());
        return insights;
}

@Transactional(readOnly = true)
public List<WeeklyTrendResponse> getWeeklyTrend(CustomUserDetails  currentUser){
        User user = getAuthenticatedUser(currentUser);
        return getWeeklyTrendByUser(user);
}



    private User getAuthenticatedUser(CustomUserDetails currentUser){
        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new UserNotFound("Authenticated User not found"));
    }

    private BigDecimal getMonthExpense(User user, YearMonth month){
    LocalDate startDate = month.atDay(1);
    LocalDate endDate = month.atEndOfMonth();

    return safe(reportRepository.getTotalExpenseByUserAndDateBetween(user,startDate,endDate));
    }

    private BigDecimal calculateAverageExpense(BigDecimal totalExpense, Long totalTransaction){
        if(totalTransaction == null || totalTransaction == 0L){
            return BigDecimal.ZERO;
        }
        return  totalExpense.divide(BigDecimal.valueOf(totalTransaction),MONEY_SCALE, RoundingMode.HALF_UP);

    }

    private List<CategorySummaryResponse> getTopCategoriesForUser(User user) {
        List<CategoryExpenseProjection> projections = reportRepository.findTopCategoriesByUser(user,PageRequest.of(0,TOP_CATEGORIES_LIMIT));
        return projections.stream().map(p->new CategorySummaryResponse(p.getCategoryName(),safe(p.getTotalAmount())))
                .toList();
    }

    private void validateMonthlyBudget(BigDecimal monthlyBudget){
        if(monthlyBudget == null || monthlyBudget.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("monthly budget must bbe greater than zero");
        }
    }

    private int getRemainingDaysInCurrentMonth(){
        LocalDate today = LocalDate.now();
        LocalDate monthEnd = YearMonth.from(today).atEndOfMonth();
        long days = ChronoUnit.DAYS.between(today,monthEnd)+1;
        return (int) Math.max(days,1);

    }

    private boolean isAboveWarningThreshold(BigDecimal currentSpent, BigDecimal monthlyBudget){
        BigDecimal spentPercentage = currentSpent
                .multiply(BigDecimal.valueOf(100))
                .divide(monthlyBudget,MONEY_SCALE,RoundingMode.HALF_UP);
        return spentPercentage.compareTo(WARNING_THRESHOLD_PERCENT) >= 0;
    }

    private String resolveSeverity(BigDecimal currentSpent, BigDecimal monthlyBudget){
        if(currentSpent.compareTo(monthlyBudget) > 0){
            return "CRITICAL";
        }

        if(isAboveWarningThreshold(currentSpent, monthlyBudget)){
            return "WARNING";
        }

        return "INFO";
    }

    private List<WeeklyTrendResponse> getWeeklyTrendByUser(User user) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(7).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<WeeklyExpenseProjection> weeklyExpenses = reportRepository.getWeeklyTrendByUserAndDateBetween(user,
                startDate,
                endDate);

        log.info(
                "Fetched weekly trend for userId={}, startDate={}, endDate={}, points={}",
                user.getId(),
                startDate,
                endDate,
                weeklyExpenses.size()
        );

        return weeklyExpenses.stream()
                .map(items->new WeeklyTrendResponse(items.getWeekLabel(),safe(items.getTotalExpense())))
                .toList();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long safeCount(Long value) {
        return value == null ? 0L : value;
    }
}
