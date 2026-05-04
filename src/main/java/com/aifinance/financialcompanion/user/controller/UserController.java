package com.aifinance.financialcompanion.user.controller;

import com.aifinance.financialcompanion.user.dto.UserProfileResponse;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Fetching current user: {}", authentication.getName());

        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        return  ResponseEntity.ok(new UserProfileResponse(
                currentUser.getName(),
                currentUser.getEmail()
        ));
    }
}
