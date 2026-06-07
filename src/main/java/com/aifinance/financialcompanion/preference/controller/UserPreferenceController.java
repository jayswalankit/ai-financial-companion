package com.aifinance.financialcompanion.preference.controller;

import com.aifinance.financialcompanion.preference.dto.UpdateUserPreferenceRequest;
import com.aifinance.financialcompanion.preference.dto.UserPreferenceResponse;
import com.aifinance.financialcompanion.preference.service.UserPreferenceService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ResponseEntity<UserPreferenceResponse> getUserPreference(@AuthenticationPrincipal  CustomUserDetails currentUser){
         UserPreferenceResponse response = userPreferenceService.getUserPreference(currentUser);
         return ResponseEntity.ok(response);

    }
    @PutMapping
    public ResponseEntity<UserPreferenceResponse> updateUserPreference(@RequestBody UpdateUserPreferenceRequest request, @AuthenticationPrincipal CustomUserDetails currentUser){
        UserPreferenceResponse response = userPreferenceService.updateUserPreference(request,currentUser);
        return ResponseEntity.ok(response);
    }
}
