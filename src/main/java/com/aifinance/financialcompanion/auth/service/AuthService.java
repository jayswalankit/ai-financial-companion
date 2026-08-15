package com.aifinance.financialcompanion.auth.service;

import com.aifinance.financialcompanion.auth.dto.*;
import com.aifinance.financialcompanion.auth.entity.PendingSignup;
import com.aifinance.financialcompanion.auth.repo.PendingSignupRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final  UserRepo userRepo;
    private final  PasswordEncoder passwordEncoder;
    private final  AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PendingSignupRepository pendingSignupRepository;


    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, OtpService otpService, PendingSignupRepository pendingSignupRepository) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.pendingSignupRepository = pendingSignupRepository;
    }

    @Transactional
    public RegisterResponse register (RegisterRequest request) {

        log.info("Signup request received for email: {}");

        String email = request.email().trim().toLowerCase();

            if (userRepo.findByEmail(email).isPresent()) {

                log.warn("Signup failed - email already exists: {}");
              throw new  EmailAlreadyExistException ("Email already exist ");
            }

            pendingSignupRepository.deleteByEmail(email);

            PendingSignup pendingSignup = new PendingSignup();
            pendingSignup.setUsername(request.username().trim());
            pendingSignup.setEmail(email);
            pendingSignup.setPasswordHash(passwordEncoder.encode(request.password()));
            pendingSignupRepository.save(pendingSignup);

        log.info(
                "Signup pending email verification for {}",
                email
        );

        otpService.sendOtp(new SendOtpRequest(email), OtpPurpose.SIGNUP);
        log.info("Signup OTP sent to {}", email);

            return  new RegisterResponse("Verification code sent", email);
    }

    public AuthResponse login(LoginRequest request){

        String email = request.email().trim().toLowerCase();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));
        // ye authentication.authenticate internally customUserDetailsService ko implement karata hai and wo apna method loadUserByUsername method se db me email khojta hai email  mila to password jo encrypted hai so match hua ya nhi


        User user = userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFound("User not found "));

        if (!user.isEmailVerified()) {
            throw new BadCredentialsException("Email verification is required");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return new  AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Transactional
    public AuthResponse verifySignup(VerifyOtpRequest request){
        String email = request.email().trim().toLowerCase();
        otpService.verifyOtp(new VerifyOtpRequest(email, request.otp(), request.purpose()));

        PendingSignup pendingSignup = pendingSignupRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("No pending signup found for this email"));

        if (userRepo.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        User user = new User();
        user.setUsername(pendingSignup.getUsername());
        user.setEmail(pendingSignup.getEmail());
        user.setPassword(pendingSignup.getPasswordHash());
        user.setRole(Role.USER);
        user.setEmailVerified(true);
        userRepo.save(user);
        pendingSignupRepository.delete(pendingSignup);
        otpService.deleteOtp(email, OtpPurpose.SIGNUP);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
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
