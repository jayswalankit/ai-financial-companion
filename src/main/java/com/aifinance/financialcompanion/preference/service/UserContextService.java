package com.aifinance.financialcompanion.preference.service;

import com.aifinance.financialcompanion.customMode.entity.CustomMode;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.preference.entity.UserPreference;
import com.aifinance.financialcompanion.preference.repo.UserPreferenceRepo;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContextService {

    private final UserPreferenceRepo userPreferenceRepo;
    private final UserRepo userRepo;

    public boolean isTripMode(CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);
        UserPreference preference = userPreferenceRepo.findByUser(user);

        if(preference == null){
            return false;
        }
        return preference.getUserMode() == UserMode.TRIP;
    }

    public boolean isMedicalMode(CustomUserDetails currentUser){
        User user = getAuthenticatedUser(currentUser);

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if(preference == null){
            return false;
        }
        return preference.getUserMode() == UserMode.MEDICAL;
    }

    public boolean isNormalMode(CustomUserDetails currentUser){
        User user = getAuthenticatedUser(currentUser);

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if(preference == null){
            return true;
        }
        return preference.getUserMode() == UserMode.NORMAL;
    }

    public boolean isSilentMode(CustomUserDetails currentUser){
        User user = getAuthenticatedUser(currentUser);

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if(preference == null){
            return false;
        }
        return preference.getNotificationMode() == NotificationMode.SILENT;
    }

    public boolean isNormalNotificationMode(CustomUserDetails currentUser){
        User user = getAuthenticatedUser(currentUser);

        UserPreference preference = userPreferenceRepo.findByUser(user);

        if(preference == null){
            return true;
        }
        return preference.getNotificationMode() == NotificationMode.NORMAL;
    }

    private UserPreference getUserPreference(
            CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);

        return userPreferenceRepo.findByUser(user);
    }

    public boolean isCustomMode(
            CustomUserDetails currentUser) {

        UserPreference preference =
                getUserPreference(currentUser);

        if (preference == null) {
            return false;
        }

        return preference.getUserMode()
                == UserMode.CUSTOM;
    }

    public CustomMode getActiveCustomMode(
            CustomUserDetails currentUser) {

        UserPreference preference =
                getUserPreference(currentUser);

        if (preference == null) {
            return null;
        }

        return preference.getActiveCustomMode();
    }

    public NotificationMode getCurrentNotificationMode(
            CustomUserDetails currentUser) {

        UserPreference preference =
                getUserPreference(currentUser);

        if (preference == null) {
            return NotificationMode.NORMAL;
        }

        if (preference.getUserMode() == UserMode.CUSTOM
                && preference.getActiveCustomMode() != null) {

            return preference
                    .getActiveCustomMode()
                    .getNotificationMode();
        }

        return preference.getNotificationMode();
    }

    private User getAuthenticatedUser(CustomUserDetails currentUser){
        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new UserNotFound("Authenticated User not found"));
    }
}
