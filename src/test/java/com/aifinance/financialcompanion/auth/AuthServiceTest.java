package com.aifinance.financialcompanion.auth;

import com.aifinance.financialcompanion.auth.dto.AuthResponse;
import com.aifinance.financialcompanion.auth.dto.LoginRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterResponse;
import com.aifinance.financialcompanion.auth.service.AuthService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    @Disabled
  void signupTest(){

        RegisterRequest  request = new RegisterRequest(
                "Ayush",
                "ayush@gmail.com",
                "ayush"
        );

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("User registered successfully", response.message());


    }

    @Test
    void loginTest(){
        LoginRequest request = new LoginRequest(
                "ayush@gmail.com",
                "ayush"
        );

        AuthResponse response = authService.login(request);

        assertNotNull(response.token());
    }

}
