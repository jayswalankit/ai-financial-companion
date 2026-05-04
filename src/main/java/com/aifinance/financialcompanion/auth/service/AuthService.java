package com.aifinance.financialcompanion.auth.service;

import com.aifinance.financialcompanion.auth.dto.AuthResponse;
import com.aifinance.financialcompanion.auth.dto.LoginRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterRequest;
import com.aifinance.financialcompanion.auth.dto.RegisterResponse;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.Role;
import com.aifinance.financialcompanion.exceptions.EmailAlreadyExistException;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import com.aifinance.financialcompanion.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    private final  UserRepo userRepo;
    private final  PasswordEncoder passwordEncoder;
    private final  AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RegisterResponse register (RegisterRequest request) {

        log.info("Signup request received for email: {}");

        String email = request.email().trim().toLowerCase();

            if (userRepo.findByEmail(email).isPresent()) {

                log.warn("Signup failed - email already exists: {}");
              throw new  EmailAlreadyExistException ("Email already exist ");
            }

            User user = new User();
            user.setUsername(request.username());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setRole(Role.USER);

            userRepo.save(user);
        log.info("User registered successfully: {}");

            return  new RegisterResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest request){

        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));

        User user = userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFound("User not found "));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return new  AuthResponse(
                token,
                user.getUsername(),
                user.getEmail()
        );
    }


}
