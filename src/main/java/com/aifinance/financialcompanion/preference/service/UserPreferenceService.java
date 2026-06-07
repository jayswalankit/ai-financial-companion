package com.aifinance.financialcompanion.preference.service;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.preference.dto.UpdateUserPreferenceRequest;
import com.aifinance.financialcompanion.preference.dto.UserPreferenceResponse;
import com.aifinance.financialcompanion.preference.entity.UserPreference;
import com.aifinance.financialcompanion.preference.repo.UserPreferenceRepo;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepo userPreferenceRepo;
    private final UserRepo userRepo;

    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreference(CustomUserDetails currentUser) {
        User user = getAuthenticatedUser(currentUser);
        log.info("Getting preference mode form userId = {}", user.getId());

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if (preference == null) {
            return new UserPreferenceResponse(UserMode.NORMAL,
                    NotificationMode.NORMAL);
        }
        return new UserPreferenceResponse(preference.getUserMode(),
                preference.getNotificationMode());

    }

    private User getAuthenticatedUser(CustomUserDetails currentUser) {
        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(() -> new UserNotFound("User not found"));
    }

    @Transactional
    public UserPreferenceResponse updateUserPreference( UpdateUserPreferenceRequest request,CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);
        log.info("Updating User preference");

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if (preference == null) {
            preference = new UserPreference();
            preference.setUser(user);
        }
        preference.setUserMode(request.userMode());
        preference.setNotificationMode(request.notificationMode());

        UserPreference savedPreferenced = userPreferenceRepo.save(preference);

        return new UserPreferenceResponse(savedPreferenced.getUserMode(),
                savedPreferenced.getNotificationMode());
    }
}
