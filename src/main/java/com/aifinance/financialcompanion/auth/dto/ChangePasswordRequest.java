package com.aifinance.financialcompanion.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank(message = "OTP is required")
        String otp,

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
                  message = "Password, must contain uppercase,lowercase,number , special characters and minimum 8 characters")
        String newPassword,

        @NotBlank(message = "Retype your new password to confirm your password")
        String confirmPassword
) {
}
