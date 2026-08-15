package com.aifinance.financialcompanion.expense.event;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationSeverity;
import com.aifinance.financialcompanion.notification.service.NotificationService;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.report.dto.BudgetStatusResponse;
import com.aifinance.financialcompanion.report.service.ReportService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseBudgetAlertListener {

    private final UserRepo userRepo;
    private final ReportService reportService;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyIfBudgetIsCritical(ExpenseCreatedEvent event) {
        userRepo.findById(event.userId()).ifPresent(user -> createCriticalBudgetAlert(user));
    }

    private void createCriticalBudgetAlert(User user) {
        BudgetStatusResponse budgetStatus = reportService.getBudgetStatus(user);
        if (!"CRITICAL".equals(budgetStatus.status())) {
            return;
        }

        notificationService.createNotification(
                new CustomUserDetails(user),
                budgetStatus.advice(),
                NotificationSeverity.CRITICAL
        );
    }
}
