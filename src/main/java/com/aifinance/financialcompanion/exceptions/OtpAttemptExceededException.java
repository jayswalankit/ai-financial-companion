package com.aifinance.financialcompanion.exceptions;

public class OtpAttemptExceededException extends RuntimeException {
    public OtpAttemptExceededException(String message) {
        super(message);
    }
}
