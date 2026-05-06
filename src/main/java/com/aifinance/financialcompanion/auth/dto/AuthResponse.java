package com.aifinance.financialcompanion.auth.dto;

public record AuthResponse(
        String token ,
        String username ,
        String email
) {
}
