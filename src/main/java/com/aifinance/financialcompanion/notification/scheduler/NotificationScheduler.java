package com.aifinance.financialcompanion.notification.scheduler;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.notification.service.NotificationService;
import com.aifinance.financialcompanion.preference.service.UserContextService;
import com.aifinance.financialcompanion.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final UserContextService userContextService;

    @Scheduled(cron = "0 0 22 * * *")

public void generateDailySummaries(){


    log.info("Running Daily Summary Scheduler");


    List<User> users = userRepo.findAll();

    log.info("Total users found = {}", users.size());

    for(User user : users){

        NotificationMode mode =
                userContextService
                        .getCurrentNotificationMode(user);

        log.info(
                "Checking userId = {}, notificationMode = {}",
                user.getId(),
                mode
        );

        if(mode != NotificationMode.SILENT){

            log.info(
                    "Skipping userId = {} because mode = {}",
                    user.getId(),
                    mode
            );

            continue;
        }

        notificationService
                .generateAndStoreDailySummary(user);

        log.info(
                "Daily summary checked completed for userId = {}",
                user.getId()
        );
    }

    log.info("Daily Summary Scheduler Completed");
}



}
