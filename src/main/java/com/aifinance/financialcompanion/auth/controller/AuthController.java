package com.aifinance.financialcompanion.auth.controller;


import com.aifinance.financialcompanion.auth.dto.AuthResponse;
import com.aifinance.financialcompanion.auth.dto.LoginRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterResponse;
import com.aifinance.financialcompanion.auth.service.AuthService;
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

}
