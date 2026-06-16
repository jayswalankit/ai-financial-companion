package com.aifinance.financialcompanion.notification.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<NotificationLog,Long> {
   List<NotificationLog> findByUser(User user);
}
