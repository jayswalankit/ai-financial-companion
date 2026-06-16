package com.aifinance.financialcompanion.customMode.service;

import com.aifinance.financialcompanion.customMode.dto.ActiveCustomModeResponse;
import com.aifinance.financialcompanion.customMode.dto.CreateCustomModeRequest;
import com.aifinance.financialcompanion.customMode.dto.CustomModeResponse;
import com.aifinance.financialcompanion.customMode.dto.UpdateCustomModeRequest;
import com.aifinance.financialcompanion.customMode.entity.CustomMode;
import com.aifinance.financialcompanion.customMode.repo.CustomModeRepo;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.preference.entity.UserPreference;
import com.aifinance.financialcompanion.preference.repo.UserPreferenceRepo;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jdk.dynalink.linker.LinkerServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomModeService {
    private final CustomModeRepo customModeRepo;
    private final UserRepo userRepo;
    private final UserPreferenceRepo userPreferenceRepo;

    @Transactional

    public CustomModeResponse createCustomMode(CreateCustomModeRequest request , CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        if(customModeRepo.existsByUserAndModeName(
                user,
                request.modeName()
        )){
            throw new RuntimeException(
                    "Custom mode already exists"
            );
        }

        CustomMode customMode = new CustomMode();
        customMode.setModeName(request.modeName());
        customMode.setNotificationMode(request.notificationMode());
        customMode.setUser(user);

        CustomMode savedMode = customModeRepo.save(customMode);

        log.info(
                "Custom mode created for userId = {}, modeName = {}",
                user.getId(),
                savedMode.getModeName());

        return new CustomModeResponse(savedMode.getId(),
                savedMode.getModeName(),
                savedMode.getNotificationMode());

    }

    @Transactional(readOnly = true)
    public List<CustomModeResponse> getAllCustomModes(
            CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);

        return customModeRepo.findByUser(user)
                .stream()
                .map(mode -> new CustomModeResponse(
                        mode.getId(),
                        mode.getModeName(),
                        mode.getNotificationMode()
                ))
                .toList();
    }

    @Transactional
    public void deleteCustomMode(Long modeId, CustomUserDetails currentUser){
        User user = getAuthenticatedUser(currentUser);

        CustomMode customMode = customModeRepo.findByIdAndUser(modeId,user)
                .orElseThrow(() -> new RuntimeException("Custom Mode not found"));

        customModeRepo.delete(customMode);
        log.info(
                "Custom mode deleted. modeId = {}, userId = {}",
                modeId,
                user.getId()
        );
    }

    @Transactional
    public CustomModeResponse updateCustomMode(
            Long modeId,
            UpdateCustomModeRequest request,
            CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);

        CustomMode customMode = customModeRepo
                .findByIdAndUser(modeId, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Custom mode not found"
                        ));

        boolean modeAlreadyExists =
                customModeRepo.existsByUserAndModeName(
                        user,
                        request.modeName()
                );

        if (modeAlreadyExists &&
                !customMode.getModeName()
                        .equalsIgnoreCase(request.modeName())) {

            throw new RuntimeException(
                    "Custom mode name already exists"
            );
        }

        customMode.setModeName(
                request.modeName()
        );

        customMode.setNotificationMode(
                request.notificationMode()
        );

        CustomMode updatedMode =
                customModeRepo.save(customMode);

        log.info(
                "Custom mode updated. modeId = {}, userId = {}",
                updatedMode.getId(),
                user.getId()
        );

        return new CustomModeResponse(
                updatedMode.getId(),
                updatedMode.getModeName(),
                updatedMode.getNotificationMode()
        );
    }

    @Transactional
    public void activateCustomMode(
            Long modeId,
            CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);

        CustomMode customMode = customModeRepo
                .findByIdAndUser(modeId, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Custom mode not found"
                        ));

        UserPreference preference =
                userPreferenceRepo.findByUser(user);

        if (preference == null) {

            preference = new UserPreference();

            preference.setUser(user);

            preference.setNotificationMode(
                    NotificationMode.NORMAL
            );
        }

        preference.setUserMode(
                UserMode.CUSTOM
        );

        preference.setActiveCustomMode(
                customMode
        );

        userPreferenceRepo.save(preference);

        log.info(
                "Custom mode activated. modeId = {}, userId = {}",
                customMode.getId(),
                user.getId()
        );
    }

    @Transactional(readOnly = true)
    public ActiveCustomModeResponse getActiveCustomMode(
            CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        UserPreference preference =
                userPreferenceRepo.findByUser(user);

        if(preference == null
                || preference.getActiveCustomMode() == null){

            throw new RuntimeException(
                    "No active custom mode found"
            );
        }

        CustomMode customMode =
                preference.getActiveCustomMode();

        return new ActiveCustomModeResponse(
                customMode.getId(),
                customMode.getModeName(),
                customMode.getNotificationMode()
        );
    }

    private User getAuthenticatedUser(CustomUserDetails currentUser){

        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()-> new UserNotFound("Authenticated User not found"));
    }
}
