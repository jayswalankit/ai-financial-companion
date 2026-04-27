package com.aifinance.financialcompanion.auth.service;

import com.aifinance.financialcompanion.auth.dto.AuthResponse;
import com.aifinance.financialcompanion.auth.dto.LoginRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterResponse;
import com.aifinance.financialcompanion.auth.entity.User;
import com.aifinance.financialcompanion.auth.enums.Role;
import com.aifinance.financialcompanion.auth.exceptions.EmailAlreadyExistException;
import com.aifinance.financialcompanion.auth.exceptions.UserNotFound;
import com.aifinance.financialcompanion.auth.repo.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final  UserRepo userRepo;
    private final  PasswordEncoder passwordEncoder;
    private final  AuthenticationManager authenticationManager;


    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public RegisterResponse register (RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

            if (userRepo.findByEmail(email).isPresent()) {
              throw new  EmailAlreadyExistException ("Email already exist ");
            }

            User user = new User();
            user.setUsername(request.username());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setRole(Role.USER);

            userRepo.save(user);

            return  new RegisterResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest request){

        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));

        User user = userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFound("User not found "));

        return new  AuthResponse(
                "dummy-token",
                user.getUsername(),
                user.getEmail()
        );
    }


}
