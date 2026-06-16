package com.aifinance.financialcompanion.customMode.controller;

import com.aifinance.financialcompanion.customMode.dto.ActiveCustomModeResponse;
import com.aifinance.financialcompanion.customMode.dto.CreateCustomModeRequest;
import com.aifinance.financialcompanion.customMode.dto.CustomModeResponse;
import com.aifinance.financialcompanion.customMode.dto.UpdateCustomModeRequest;
import com.aifinance.financialcompanion.customMode.service.CustomModeService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/custom-modes")
@RequiredArgsConstructor
public class CustomModeController {

    private final CustomModeService customModeService;

    @PostMapping
    public ResponseEntity<CustomModeResponse> createCustomMode(
            @RequestBody CreateCustomModeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        CustomModeResponse response =
                customModeService.createCustomMode(
                        request,
                        currentUser
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomModeResponse>> getAllCustomModes(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        List<CustomModeResponse> response =
                customModeService.getAllCustomModes(
                        currentUser
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ActiveCustomModeResponse>
    getActiveCustomMode(

            @AuthenticationPrincipal
            CustomUserDetails currentUser){

        ActiveCustomModeResponse response =
                customModeService
                        .getActiveCustomMode(
                                currentUser
                        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{modeId}")
    public ResponseEntity<String> deleteCustomMode(
            @PathVariable Long modeId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        customModeService.deleteCustomMode(
                modeId,
                currentUser
        );

        return ResponseEntity.ok(
                "Custom mode deleted successfully"
        );
    }

    @PutMapping("/{modeId}")
    public ResponseEntity<CustomModeResponse> updateCustomMode(
            @PathVariable Long modeId,
            @RequestBody UpdateCustomModeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        CustomModeResponse response =
                customModeService.updateCustomMode(
                        modeId,
                        request,
                        currentUser
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{modeId}/activate")
    public ResponseEntity<String> activateCustomMode(
            @PathVariable Long modeId,
            @AuthenticationPrincipal
            CustomUserDetails currentUser) {

        customModeService.activateCustomMode(
                modeId,
                currentUser
        );

        return ResponseEntity.ok(
                "Custom mode activated successfully"
        );
    }



}
