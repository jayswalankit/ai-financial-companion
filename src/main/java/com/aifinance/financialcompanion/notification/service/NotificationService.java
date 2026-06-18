package com.aifinance.financialcompanion.notification.service;

import com.aifinance.financialcompanion.budget.service.BudgetService;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.NotificationSeverity;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.notification.dto.DailySummaryResponse;
import com.aifinance.financialcompanion.notification.dto.MonthlySummaryResponse;
import com.aifinance.financialcompanion.notification.dto.NotificationResponse;
import com.aifinance.financialcompanion.notification.entity.NotificationLog;
import com.aifinance.financialcompanion.notification.repo.NotificationRepo;
import com.aifinance.financialcompanion.preference.service.UserContextService;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.report.dto.InsightResponse;
import com.aifinance.financialcompanion.report.service.ReportService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final ReportService reportService;
    private final BudgetService budgetService;
    private final UserContextService userContextService;

    @Transactional
    public NotificationResponse createNotification(CustomUserDetails currentUser, String message, NotificationSeverity severity){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Creating notification for userId = {},severity = {}",user.getId(),severity);

        NotificationLog notification = new NotificationLog(user,message,severity,null);

        NotificationLog savedNotification = notificationRepo.save(notification);

        return  new NotificationResponse(savedNotification.getMessage(),
                savedNotification.getSeverity().name(),
                savedNotification.getSentAt());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(CustomUserDetails currentUser){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Gettinng notification for userId = {}",user.getId());

       List<NotificationLog> notifications = notificationRepo.findByUser(user);

       if(notifications.isEmpty()){
            return List.of();
       }
       return notifications.stream()
                .map(notification->new NotificationResponse(notification.getMessage(), notification.getSeverity().name(),notification.getSentAt()))
                .toList();

    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getPrioritizedNotifications(CustomUserDetails currentUser){

        User user = getAuthenticatedUSer(currentUser);

        return notificationRepo.findByUser(user)
                .stream()
                .sorted(
                        Comparator
                                .comparing(NotificationLog::getSeverity)
                                .thenComparing(
                                        NotificationLog::getSentAt,
                                        Comparator.reverseOrder()
                                )
                )
                .map(notification ->
                        new NotificationResponse(
                                notification.getMessage(),
                                notification.getSeverity().name(),
                                notification.getSentAt()
                        )
                )
                .toList();
    }

    @Transactional
    public MonthlySummaryResponse generateMonthlySummaryResponse(CustomUserDetails currentUser){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Generating DailyNotification for userId = {} ",user.getId());

        YearMonth currentMonth = YearMonth.now();
        LocalDate  startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal totalSpent = reportService.getCurrentMonthExpense(user);

        BigDecimal monthlyBudget = budgetService.getCurrentMonthBudget(user);

        BigDecimal remainingBudget = monthlyBudget.subtract(totalSpent);

        String budgetStatus;

        if(monthlyBudget.compareTo(BigDecimal.ZERO) == 0){
            budgetStatus = "BUDGET_NOT _SET";
        }
        else{
            BigDecimal usagePercentage = totalSpent
                    .multiply(BigDecimal.valueOf(100)).divide(monthlyBudget,2, RoundingMode.HALF_UP);

            if(usagePercentage.compareTo(BigDecimal.valueOf(100)) >= 0){
                budgetStatus = "CRITICAL";
            } else if (usagePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
               budgetStatus = "WARNING";
            }
            else{
                budgetStatus = "SAFE";
            }
        }
          String topCategory = reportService.getCurrentMonthTopCategory(user);

        return new MonthlySummaryResponse(totalSpent,remainingBudget,budgetStatus,topCategory);

    }

    @Transactional(readOnly = true)
    public DailySummaryResponse generateDailySummaryResponse(CustomUserDetails currentUser){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Generating daily basic response");

        LocalDate currentDate = LocalDate.now();
        BigDecimal todaySpent = reportService.getTodaySpent(user,currentDate);

        String todayTopCategory = reportService.getTodayTopCategory(user);

        long expenseCount = reportService.getTodayExpenseCount(user);

        return new DailySummaryResponse(
                todaySpent,
                todayTopCategory,
                expenseCount,
                currentDate
        );
    }

    @Transactional
    public void createNotificationsFromInsights(CustomUserDetails currentUser, List<InsightResponse> insights) {

        if (insights == null || insights.isEmpty()) {
            return;
        }

        for (InsightResponse insight : insights) {

            NotificationSeverity severity;

            try {
                severity = NotificationSeverity.valueOf(
                        insight.severity().toUpperCase()
                );
            } catch (IllegalArgumentException e) {

                log.warn(
                        "Invalid severity '{}' for insight '{}'",
                        insight.severity(),
                        insight.message()
                );

                severity = NotificationSeverity.INFO;
            }

            createNotification(
                    currentUser,
                    insight.message(),
                    severity
            );
        }

        log.info(
                "Created {} notifications for userId = {}",
                insights.size(),
                currentUser.getUserId()
        );
    }

    public boolean shouldSendInstantNotification(
            CustomUserDetails currentUser){

        return userContextService
                .getCurrentNotificationMode(currentUser)
                == NotificationMode.NORMAL;
    }

    @Transactional
    public void generateAndStoreDailySummary(User user){

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay =
                today.atStartOfDay();

        LocalDateTime endOfDay =
                today.atTime(23,59,59);

        boolean alreadyGenerated =
                notificationRepo
                        .existsByUserAndMessageContainingAndSentAtBetween(
                                user,
                                "Daily Summary",
                                startOfDay,
                                endOfDay
                        );

        if(alreadyGenerated){

            log.info(
                    "Daily summary already exists for userId={}",
                    user.getId()
            );

            return;
        }

        BigDecimal todaySpent =
                reportService.getTodaySpent(
                        user,
                        today
                );

        String todayTopCategory =
                reportService.getTodayTopCategory(user);

        long expenseCount =
                reportService.getTodayExpenseCount(user);

        String message = String.format(
                """
                Daily Summary
    
                Today Spent: %s
    
                Top Category: %s
    
                Transactions: %d
                """,
                todaySpent,
                todayTopCategory,
                expenseCount
        );

        NotificationLog notification =
                new NotificationLog(
                        user,
                        message,
                        NotificationSeverity.INFO,
                        null
                );

        notificationRepo.save(notification);

        log.info(
                "Daily summary stored for userId={}",
                user.getId()
        );
    }


    private User getAuthenticatedUSer(CustomUserDetails currentUser){
        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new UserNotFound("User not found"));
    }
}
