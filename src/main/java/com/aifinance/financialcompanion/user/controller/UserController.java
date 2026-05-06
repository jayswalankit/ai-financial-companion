package com.aifinance.financialcompanion.user.controller;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.repo.UserRepo;
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

    private final UserRepo userRepo;

    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Fetching current user: {}", authentication.getName());

        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found"));

        return ResponseEntity.ok(new UserProfileResponse(
                user.getUsername(),
                user.getEmail()
        ));
    }
}
