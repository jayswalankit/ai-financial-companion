package com.aifinance.financialcompanion.auth.controller;


import com.aifinance.financialcompanion.auth.dto.*;
import com.aifinance.financialcompanion.auth.service.AuthService;
import com.aifinance.financialcompanion.opt.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

   public  AuthController (AuthService authService){
        this.authService = authService;
    }

  @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> signup(@RequestBody @Valid RegisterRequest request){

        RegisterResponse response = authService.register(request);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
  }

  @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody @Valid LoginRequest request){
       AuthResponse response = authService.login(request);
       return new ResponseEntity<>(response,HttpStatus.OK);
  }

    @PostMapping("/verify-signup")
    public ResponseEntity<AuthResponse>
    verifySignup(
            @Valid
            @RequestBody
            VerifyOtpRequest request){

        return ResponseEntity.ok(
                authService.verifySignup(
                        request
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<RegisterResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(authService.forgetPassword(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<RegisterResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(authService.changePassword(request));
    }

}
