package com.aifinance.financialcompanion.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest (
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "email is reequired")
        String email,

        @NotBlank (message = "password is required")
        @Size(min = 9,message = "password must be at least  9 characters")
        String password

){

}
