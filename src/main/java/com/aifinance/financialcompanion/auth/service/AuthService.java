package com.aifinance.financialcompanion.auth.service;

import com.aifinance.financialcompanion.auth.dto.*;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.OtpPurpose;
import com.aifinance.financialcompanion.enums.Role;
import com.aifinance.financialcompanion.exceptions.EmailAlreadyExistException;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.opt.dto.SendOtpRequest;
import com.aifinance.financialcompanion.opt.dto.VerifyOtpRequest;
import com.aifinance.financialcompanion.opt.entity.OtpVerification;
import com.aifinance.financialcompanion.opt.service.OtpService;
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
    private final OtpService otpService;


    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService,OtpService otpService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;    }

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
            user.setEmailVerified(false);

            userRepo.save(user);

        log.info(
                "User registered successfully. userId={}",
                user.getId()
        );

        otpService.sendOtp(new SendOtpRequest(user.getEmail()), OtpPurpose.SIGNUP);
        log.info("User registered successfully: {}");

            return  new RegisterResponse("User registered successfully", user.getEmail());
    }

    public AuthResponse login(LoginRequest request){

        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));
        // ye authentication.authenticate internally customUserDetailsService ko implement karata hai and wo apna method loadUserByUsername method se db me email khojta hai email  mila to password jo encrypted hai so match hua ya nhi


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

    public AuthResponse verifySignup(VerifyOtpRequest request){
        otpService.verifyOtp(request);

        User user = userRepo.findByEmail(request.email()).orElseThrow(
                ()->new UserNotFound("User not  found")
        );
        user.setEmailVerified(true);
        userRepo.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail()
        );
    }

    public RegisterResponse forgetPassword(ForgotPasswordRequest request){
        String email = request.email().trim().toLowerCase();

        // checking from the db that email is same
        User user = userRepo.findByEmail(email).orElseThrow(()->new UserNotFound("User not found"));

        // otp service class is reused...
        otpService.sendOtp(new SendOtpRequest(user.getEmail()),OtpPurpose.PASSWORD_RESET);

        log.info("password reset otp sent to {}",email);

        return new RegisterResponse(
                "Password reset otp sent successfully",
                user.getEmail()
        );
    }

    public RegisterResponse changePassword(ChangePasswordRequest request){
        String email = request.email().trim().toLowerCase();

        User user = userRepo.findByEmail(email).orElseThrow(()->new UserNotFound("User not found"));

        if(!request.newPassword().equals(request.confirmPassword())){
            throw new IllegalArgumentException("Password doesn't match");
        }

        VerifyOtpRequest verifyRequest = new VerifyOtpRequest(
                email,
                request.otp(),
                OtpPurpose.PASSWORD_RESET
        );

        otpService.verifyOtp(verifyRequest);

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepo.save(user);

        otpService.deleteOtp(
                email,
                OtpPurpose.PASSWORD_RESET
        );

        log.info(
                "Password changed successfully for {}",
                email
        );

        return new RegisterResponse(
                "Password changed successfully.",
                email
        );

    }




}
