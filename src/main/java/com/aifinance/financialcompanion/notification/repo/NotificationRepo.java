package com.aifinance.financialcompanion.notification.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.notification.dto.NotificationResponse;
import com.aifinance.financialcompanion.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepo extends JpaRepository<NotificationLog,Long> {
   List<NotificationLog> findByUser(User user);

    boolean existsByUserAndMessageContainingAndSentAtBetween(
            User user,
            String message,
            LocalDateTime start,
            LocalDateTime end
    );

    List<NotificationResponse> findByUserOrderBySentAtDesc(User user);
}
