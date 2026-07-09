package com.aifinance.financialcompanion.opt.dto;

import com.aifinance.financialcompanion.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invvalid email")
        String email,

        @Pattern(
                regexp ="\\d{6}",
                message = "Otp must be exactly 6 digits"
        )
        String otp,

        @NotNull(message = "Purpose is required")
        OtpPurpose purpose
) {
}
