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
import com.aifinance.financialcompanion.report.dto.BudgetStatusResponse;
import com.aifinance.financialcompanion.report.dto.InsightResponse;
import com.aifinance.financialcompanion.report.service.ReportService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Transactional
    public NotificationResponse createNotification(CustomUserDetails currentUser, String message, NotificationSeverity severity){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Creating notification for userId = {},severity = {}",user.getId(),severity);

        NotificationLog notification = new NotificationLog(user,message,severity,null);

        NotificationLog savedNotification = notificationRepo.save(notification);

        if(shouldSendInstantNotification(currentUser)){
            sendEmail(user,"AI Financial Companion",message);

            log.info("Instant email sent to {}",
                    user.getEmail());
        }
        else{
            log.info(
                    "Silent mode enabled. Instant email skipped for userId={}",
                    user.getId()
            );
        }

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

    @Transactional(readOnly = true)
    public MonthlySummaryResponse generateMonthlySummaryResponse(CustomUserDetails currentUser) {

        User user = getAuthenticatedUSer(currentUser);

        log.info(
                "Generating Monthly Summary for userId={}",
                user.getId()
        );

        BigDecimal totalSpent =
                reportService.getCurrentMonthExpense(user);

        BigDecimal monthlyBudget =
                budgetService.getCurrentMonthBudget(user);

        BigDecimal remainingBudget =
                monthlyBudget.subtract(totalSpent);

        BudgetStatusResponse budgetStatusResponse =
                reportService.getBudgetStatus(currentUser);

        String topCategory =
                reportService.getCurrentMonthTopCategory(user);

        return new MonthlySummaryResponse(
                totalSpent,
                remainingBudget,
                budgetStatusResponse.status(),
                topCategory
        );
    }

    ///  create kyu ki upar wale me current user user kar rhe ahi and expense.service an scheduler me humne user use kiya hai is liye ....
    @Transactional(readOnly = true)
    public MonthlySummaryResponse generateMonthlySummaryResponse(User user) {

        log.info(
                "Generating Monthly Summary for userId={}",
                user.getId()
        );

        BigDecimal totalSpent =
                reportService.getCurrentMonthExpense(user);

        BigDecimal monthlyBudget =
                budgetService.getCurrentMonthBudget(user);

        BigDecimal remainingBudget =
                monthlyBudget.subtract(totalSpent);

        BudgetStatusResponse budgetStatusResponse =
                reportService.getBudgetStatus(user);

        String topCategory =
                reportService.getCurrentMonthTopCategory(user);

        return new MonthlySummaryResponse(
                totalSpent,
                remainingBudget,
                budgetStatusResponse.status(),
                topCategory
        );
    }
    @Transactional(readOnly = true)
    public DailySummaryResponse generateDailySummaryResponse(CustomUserDetails currentUser){

        User user = getAuthenticatedUSer(currentUser);
        log.info("Generating daily basic response");

        LocalDate currentDate = LocalDate.now();
        BigDecimal todaySpent = reportService.getTodaySpent(user,currentDate);

        String todayTopCategory = reportService.getTodayTopCategory(user);

        long expenseCount = reportService.getTodayExpenseCount(user);

        BudgetStatusResponse budgetStatus =
                reportService.getBudgetStatus(user);

        return new DailySummaryResponse(
                todaySpent,
                todayTopCategory,
                expenseCount,
                currentDate,
                budgetStatus.recommendedLimit()
        );
    }

    ///  create kyu ki upar wale me current user user kar rhe ahi and expense.service an scheduler me humne user use kiya hai is liye ....
    @Transactional(readOnly = true)
    public DailySummaryResponse generateDailySummaryResponse(User user) {

        log.info("Generating daily summary response for userId={}", user.getId());

        LocalDate currentDate = LocalDate.now();

        BigDecimal todaySpent =
                reportService.getTodaySpent(user, currentDate);

        String todayTopCategory =
                reportService.getTodayTopCategory(user);

        long expenseCount =
                reportService.getTodayExpenseCount(user);

        BudgetStatusResponse budgetStatus =
                reportService.getBudgetStatus(user);

        return new DailySummaryResponse(
                todaySpent,
                todayTopCategory,
                expenseCount,
                currentDate,
                budgetStatus.recommendedLimit()
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

    public boolean shouldSendInstantNotification(CustomUserDetails currentUser){
        return userContextService
                .getCurrentNotificationMode(currentUser)
                == NotificationMode.NORMAL;
    }

    private void sendEmail(User user,String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info(
                    "Email sent successfully to {}",
                    user.getEmail()
            );
        } catch (Exception e) {
           log.error("Failed to send email to {}",user.getEmail(),e);
        }
    }

    @Transactional(readOnly = true)
    public void sendFinancialSummary(User user) {

        DailySummaryResponse daily =
                generateDailySummaryResponse(user);

        MonthlySummaryResponse monthly =
                generateMonthlySummaryResponse(user);

        String message = String.format(
                """
                 AI Financial Companion
    
                =======================================
    
                 DAILY SUMMARY
    
                Today's Spending : %s
    
                Top Category : %s
    
                Transactions : %d
    
                Recommended Daily Limit : %s
    
                =======================================
    
                 MONTHLY SUMMARY
    
                Monthly Budget : %s
    
                Total Spent : %s
    
                Remaining Budget : %s
    
                Budget Status : %s
    
                Top Category : %s
                """,

                daily.todaySpent(),
                daily.todayTopCategory(),
                daily.expenseCount(),
                daily.recommendedDailyLimit(),

                budgetService.getCurrentMonthBudget(user),
                monthly.totalSpent(),
                monthly.remainingBudget(),
                monthly.budgetStatus(),
                monthly.topCategory()
        );

        sendEmail(
                user,
                "AI Financial Companion - Financial Summary",
                message
        );

        log.info(
                "Financial summary email sent to userId={}",
                user.getId()
        );
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

        BudgetStatusResponse budgetStatus =
                reportService.getBudgetStatus(user);


        String message = String.format(
                """
                Daily Summary
    
                Today Spent: %s
    
                Top Category: %s
    
                Transactions: %d
                
                Recommended Daily Limit: %s
                """,
                todaySpent,
                todayTopCategory,
                expenseCount,
                budgetStatus.recommendedLimit()
        );

        NotificationLog notification =
                new NotificationLog(
                        user,
                        message,
                        NotificationSeverity.INFO,
                        null
                );

        notificationRepo.save(notification);

        sendEmail(user,"AI Financial Companion - Daily Summary",message);

        log.info(
                "Daily summary stored for userId={}",
                user.getId()
        );
    }

    @Transactional
    public void generateAndStoreMonthlySummary(User user) {

        LocalDateTime startOfMonth =
                YearMonth.now()
                        .atDay(1)
                        .atStartOfDay();

        LocalDateTime endOfMonth =
                YearMonth.now()
                        .atEndOfMonth()
                        .atTime(23,59,59);

        boolean alreadyGenerated =
                notificationRepo
                        .existsByUserAndMessageContainingAndSentAtBetween(
                                user,
                                "Monthly Summary",
                                startOfMonth,
                                endOfMonth
                        );

        if(alreadyGenerated){

            log.info(
                    "Monthly summary already exists for userId={}",
                    user.getId()
            );

            return;
        }

        BigDecimal totalSpent =
                reportService.getCurrentMonthExpense(user);

        BigDecimal budget =
                budgetService.getCurrentMonthBudget(user);

        BigDecimal remaining =
                budget.subtract(totalSpent);

        String topCategory =
                reportService.getCurrentMonthTopCategory(user);

        String message = String.format(
                """
                Monthly Summary
    
                Total Spent: %s
    
                Budget: %s
    
                Remaining: %s
    
                Top Category: %s
                """,
                totalSpent,
                budget,
                remaining,
                topCategory
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
                "Monthly summary stored for userId={}",
                user.getId()
        );
    }

    private User getAuthenticatedUSer(CustomUserDetails currentUser){
        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new UserNotFound("User not found"));
    }
}