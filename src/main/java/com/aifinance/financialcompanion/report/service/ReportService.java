package com.aifinance.financialcompanion.report.service;

import com.aifinance.financialcompanion.budget.entity.MonthlyBudget;
import com.aifinance.financialcompanion.budget.repo.MonthlyBudgetRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.expense.entity.Expense;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.report.dto.*;
import com.aifinance.financialcompanion.report.projection.CategoryExpenseProjection;
import com.aifinance.financialcompanion.report.projection.CategoryGrowthProjection;
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
    private final MonthlyBudgetRepository monthlyBudgetRepo;
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

    YearMonth currentMonth = YearMonth.now();

    BigDecimal currentMonthExpense = getMonthExpense(user,currentMonth);

    BigDecimal previousMonthExpense = getMonthExpense(user,currentMonth.minusMonths(1));

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
 public BudgetStatusResponse getBudgetStatus(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        MonthlyBudget currentBudget = getCurrentMonthBudget(user);

    BigDecimal currentSpent = getMonthExpense(user,YearMonth.now());

    if(currentBudget == null){
        return new BudgetStatusResponse(
                BigDecimal.ZERO,
                currentSpent,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "BUDGET_NOT_SET",
                "Set Your Budget"
        );
    }

    BigDecimal monthlyBudget = currentBudget.getBudgetAmount();

        BigDecimal remainingBudget = monthlyBudget.subtract(currentSpent);

        int remainingDays  = getRemainingDaysInCurrentMonth();

        BigDecimal recommendedDailyLimit = remainingBudget.divide(BigDecimal.valueOf(remainingDays),MONEY_SCALE,RoundingMode.HALF_UP);

        String status = resolveSeverity(currentSpent,monthlyBudget);

        String advice= getBudgetAdvice(currentSpent,monthlyBudget,recommendedDailyLimit);

        log.info("Generated budget status for userId = {}, monthlyBudget = {}, currentSpent = {}, remainingBudget = {}, remainingDays = {}",user.getId(),
                monthlyBudget,
                currentSpent,
                remainingBudget,
                remainingDays);

        return new BudgetStatusResponse(
                monthlyBudget,
                currentSpent,
                remainingBudget,
                recommendedDailyLimit,
                status,
                advice
        );
}

@Transactional(readOnly = true)
public List<InsightResponse> generateBasicInsights(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);
        BigDecimal currentSpent = getMonthExpense(user,YearMonth.now());

        MonthlyBudget currentBudget = getCurrentMonthBudget(user);

    if(currentBudget == null){
        return List.of(
                new InsightResponse(
                        "Set a monthly budget to unlock smarter spending insights.","INFO"
                )
        );
    }

        BigDecimal monthlyBudget = currentBudget.getBudgetAmount();

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
            CategorySummaryResponse topCategory = topCategories.getFirst();
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

@Transactional(readOnly = true)
public MonthlyComparisonResponse getMonthlyComparison(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal currentMonthExpense = getMonthExpense(user,currentMonth);
        BigDecimal previousMonthExpense = getMonthExpense(user,previousMonth);

        BigDecimal percentageChange;

        if(previousMonthExpense.compareTo(BigDecimal.ZERO) == 0){
            percentageChange = BigDecimal.valueOf(100);
        }
        else{
        percentageChange = currentMonthExpense.subtract(previousMonthExpense).divide(previousMonthExpense,MONEY_SCALE,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        String trend;

        int comparison = currentMonthExpense.compareTo(previousMonthExpense);

        if(comparison > 0){
            trend = "INCREASED";
        } else if (comparison < 0) {
            trend = "DECREASED";
        }
        else{
            trend = "SAME";
        }

        log.info("Generating MonthlyComparison of userId = {}",user.getId());

        return new MonthlyComparisonResponse(currentMonthExpense,
                previousMonthExpense,
                percentageChange,
                trend);
}

@Transactional(readOnly = true)
public FinancialHealthResponse getFinancialHealth(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        YearMonth currentMonth = YearMonth.now();
        BigDecimal totalSpent = getMonthExpense(user,currentMonth);

        MonthlyBudget currentBudget = getCurrentMonthBudget(user);

        if(currentBudget == null){
            return new FinancialHealthResponse(totalSpent,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "Budget Not SET");
        }

        BigDecimal monthlyBudget = currentBudget.getBudgetAmount();

        BigDecimal remainingBudget = monthlyBudget.subtract(totalSpent);

        BigDecimal budgetUsagePercentage = totalSpent.multiply(BigDecimal.valueOf(100)).divide(monthlyBudget,MONEY_SCALE,RoundingMode.HALF_UP);

        String financialStatus;

        if(budgetUsagePercentage.compareTo(BigDecimal.valueOf(50)) <= 0){
            financialStatus = "SAFE";

        } else if (budgetUsagePercentage.compareTo(BigDecimal.valueOf(75)) <= 0) {
            financialStatus = "MODERATE";
        } else if (budgetUsagePercentage.compareTo(BigDecimal.valueOf(90)) <= 0) {
            financialStatus = "WARNING";
        }
        else{
            financialStatus = "RISKY";
        }

        log.info("GENERATING  financialHealth for userId = {}, monthlyBudget ={}, remainingBudget = {}, financialStatus = {}",user.getId(),monthlyBudget,remainingBudget,financialStatus);

        return new FinancialHealthResponse(totalSpent,
                monthlyBudget,
                remainingBudget,
                budgetUsagePercentage,
                financialStatus);
}

@Transactional(readOnly = true)
public SpendingPatternResponse getSpendingPattern(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        YearMonth currentMonth = YearMonth.now();

        List<CategoryExpenseProjection>allTimeTopCategories = reportRepository.findTopCategoriesByUser(user,PageRequest.of(0,1));
         String allTimeHighestSpendingCategory = "Not spending Yet";
         BigDecimal allTimeHighestSpendingAmount = BigDecimal.ZERO;

         if(!allTimeTopCategories.isEmpty()){
             CategoryExpenseProjection allTimeTopCategory = allTimeTopCategories.getFirst();
             allTimeHighestSpendingCategory = allTimeTopCategory.getCategoryName();
             allTimeHighestSpendingAmount = allTimeTopCategory.getTotalAmount();
         }

         LocalDate startDate = currentMonth.atDay(1);
         LocalDate endDate = currentMonth.atEndOfMonth();

         List<CategoryExpenseProjection>monthlyTopCategories = reportRepository.findTopCategoriesByUserAndDateBetween(user,startDate,endDate,PageRequest.of(0,1));
         String currentMonthHighestCategory = "Not spending Yet";
         BigDecimal currentMonthHighestCategoryAmount = BigDecimal.ZERO;

         if(!monthlyTopCategories.isEmpty()){
             CategoryExpenseProjection monthlyTopCategory = monthlyTopCategories.getFirst();
             currentMonthHighestCategory = monthlyTopCategory.getCategoryName();
             currentMonthHighestCategoryAmount = safe(monthlyTopCategory.getTotalAmount());
         }

         BigDecimal monthTotalExpense = safe(reportRepository.getTotalExpenseByUserAndDateBetween(user,startDate,endDate));
         int dayPassed = LocalDate.now().getDayOfMonth();

         BigDecimal averageDailyExpenses = monthTotalExpense.divide(BigDecimal.valueOf(dayPassed),MONEY_SCALE,RoundingMode.HALF_UP);

         BigDecimal highestSingleExpense = safe(reportRepository.getHighestExpenseByUserAndDateBetween(user,startDate,endDate));

         LocalDate highestExpenseDate = null;

         Expense topExpense = reportRepository.findTopExpenseByUserAndDateBetween(user,startDate,endDate);

         if(topExpense != null){
             highestExpenseDate = topExpense.getExpenseDate();
         }

         log.info("Generated spending pattern for userId = {}",user.getId());

         return new SpendingPatternResponse(allTimeHighestSpendingCategory,
                 allTimeHighestSpendingAmount,
                 currentMonthHighestCategory,
                 currentMonthHighestCategoryAmount,
                 averageDailyExpenses,
                 highestSingleExpense,
                 highestExpenseDate);
}

@Transactional(readOnly = true)
public List<CategoryGrowthResponse> getCategoryGrowth(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        YearMonth currentMonth = YearMonth.now();
        LocalDate currentStartDate = currentMonth.atDay(1);
        LocalDate currentEndDate = currentMonth.atEndOfMonth();

        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate previousStartDate = previousMonth.atDay(1);
        LocalDate previousEndDate = previousMonth.atEndOfMonth();

        List<CategoryGrowthProjection> projections = reportRepository.getCategoryGrowthComparison(user,currentStartDate,currentEndDate,previousStartDate,previousEndDate);

    log.info(
            "Fetched category growth analysis for userId = {}, totalCategories = {}",
            user.getId(),
            projections.size()
    );

    return projections.stream()
            .map(p -> {BigDecimal currentMonthAmount = safe(p.getCurrentMonthAmount());
            BigDecimal previousMonthAmount = safe(p.getPreviousMonthAmount());
            BigDecimal growthPercentage;
            if(previousMonthAmount.compareTo(BigDecimal.ZERO) == 0){
                if(currentMonthAmount.compareTo(BigDecimal.ZERO) > 0){
                    growthPercentage = BigDecimal.valueOf(100);
                }
                else{
                    growthPercentage = BigDecimal.ZERO;
                }

            }
            else{
                growthPercentage = currentMonthAmount.subtract(previousMonthAmount).multiply(BigDecimal.valueOf(100)).divide(previousMonthAmount,MONEY_SCALE,RoundingMode.HALF_UP);
            }

            String trend;

            int comparison = currentMonthAmount.compareTo(previousMonthAmount);

            if(comparison < 0){
               trend = "DECREASED";
            } else if (comparison > 0) {
                trend = "INCREASED";
            }
            else{
                trend = "SAME";
            }

            return  new CategoryGrowthResponse(p.getCategoryName(),
                    currentMonthAmount,
                    previousMonthAmount,
                    growthPercentage,
                    trend);
            })
            .toList();

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

    private String getBudgetAdvice(BigDecimal currentSpent, BigDecimal monthlyBudget, BigDecimal recommendedLimit){

        if(currentSpent.compareTo(monthlyBudget)> 0){
            return "You have exceeded your monthly budget. Reduce discretionary spending";
        }

        if(isAboveWarningThreshold(currentSpent,monthlyBudget)){
            return "You are close to your monthly budget. Try spending less than rupee" + recommendedLimit + "per day.";
        }

        return "You are spending withing budget";
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

    private MonthlyBudget getCurrentMonthBudget(User user){
        YearMonth currentMonth = YearMonth.now();

        Integer month = currentMonth.getMonthValue();
        Integer year = currentMonth.getYear();
        return monthlyBudgetRepo.findByUserIdAndMonthAndYear(user.getId(), month,year);

    }
}
