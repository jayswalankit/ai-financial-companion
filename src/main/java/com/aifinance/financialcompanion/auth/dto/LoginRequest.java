package com.aifinance.financialcompanion.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
       @NotBlank(message = "Email must be required")
       @Email
       String email ,

       @NotBlank(message = "Password must be required")
       String password
) {
}
