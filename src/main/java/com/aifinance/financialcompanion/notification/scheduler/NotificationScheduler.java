package com.aifinance.financialcompanion.notification.scheduler;

import com.aifinance.financialcompanion.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    @Scheduled(cron = "0 0 22 * * *")
    public void generateDailySummary(){

    }



}
