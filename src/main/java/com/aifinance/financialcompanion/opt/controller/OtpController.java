package com.aifinance.financialcompanion.opt.controller;

import com.aifinance.financialcompanion.enums.OtpPurpose;
import com.aifinance.financialcompanion.opt.dto.OtpResponse;
import com.aifinance.financialcompanion.opt.dto.SendOtpRequest;
import com.aifinance.financialcompanion.opt.dto.VerifyOtpRequest;
import com.aifinance.financialcompanion.opt.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            @RequestParam OtpPurpose purpose
    ) {

        return ResponseEntity.ok(
                otpService.sendOtp(request, purpose)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        return ResponseEntity.ok(
                otpService.verifyOtp(request)
        );
    }

    @PostMapping("/resend")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody SendOtpRequest request,
            @RequestParam OtpPurpose purpose
    ) {

        return ResponseEntity.ok(
                otpService.resendOtp(request, purpose)
        );
    }
}
