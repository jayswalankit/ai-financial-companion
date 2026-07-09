package com.aifinance.financialcompanion.opt.service;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.OtpPurpose;
import com.aifinance.financialcompanion.exceptions.InvalidOtpException;
import com.aifinance.financialcompanion.exceptions.OtpExpiredException;
import com.aifinance.financialcompanion.exceptions.OtpNotFoundException;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.opt.dto.OtpResponse;
import com.aifinance.financialcompanion.opt.dto.SendOtpRequest;
import com.aifinance.financialcompanion.opt.dto.VerifyOtpRequest;
import com.aifinance.financialcompanion.opt.entity.OtpVerification;
import com.aifinance.financialcompanion.opt.repo.OtpRepo;
import com.aifinance.financialcompanion.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // for generating random otp...
    private String generateRandomOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }

    /// send  otp

    @Transactional
    public OtpResponse sendOtp(SendOtpRequest request, OtpPurpose purpose){

        User user = userRepo.findByEmail(request.email())
                .orElseThrow(()->new UserNotFound("User  not found"));

        // yaha pe if else use karke exist check nhi kiye kyu ki isse 2 queries chlti db me....
        otpRepo.deleteByEmailAndPurpose(user.getEmail(),purpose);

        String plainOtp = generateRandomOtp();

        String encodedOtp = passwordEncoder.encode(plainOtp);

        OtpVerification otp = OtpVerification.builder()
                .email(user.getEmail())
                .otp(encodedOtp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .attemps(0)
                .build();

        otpRepo.save(otp);
        sendOtpEmail(user.getEmail(),plainOtp,purpose);
        log.info("Otp send Successfully for{}",user.getEmail());

        return new OtpResponse(true,
                "Opt sent Successfully");

    }

    private void sendOtpEmail(String email,
                              String plainOtp,
                              OtpPurpose purpose) {

        String subject;

        switch (purpose) {

            case SIGNUP -> subject = "Verify Your Email";

            case LOGIN -> subject = "Login OTP";

            case PASSWORD_RESET -> subject = "Password Reset OTP";

            default -> subject = "OTP";
        }

        String body = String.format("""
            AI Financial Companion

            Your OTP is

            %s

            It is valid for only 5 minutes.

            Do not share this OTP with anyone.
            """, plainOtp);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }
    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request){
         OtpVerification otpVerification = otpRepo.
                 findTopByEmailAndPurposeOrderByCreatedAtDesc(
                         request.email(),
                         request.purpose()
                 ).orElseThrow(()->new OtpNotFoundException("Otp not found"));

        if (otpVerification.isVerified()) {
            throw new InvalidOtpException( "OTP already used");
        }

        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {

            otpRepo.delete(otpVerification);

            throw new OtpExpiredException("OTP has expired");
        }

        if (otpVerification.getAttemps() >= 5) {

            otpRepo.delete(otpVerification);

            throw new OtpExpiredException("Maximum OTP attempts exceeded");
        }

        boolean matched =
                passwordEncoder.matches(
                        request.otp(),
                        otpVerification.getOtp()
                );

        if (!matched) {

            otpVerification.setAttemps(
                    otpVerification.getAttemps() + 1
            );

            otpRepo.save(otpVerification);

            throw new InvalidOtpException("Invalid OTP");
        }

        otpVerification.setVerified(true);

        otpRepo.save(otpVerification);

        log.info(
                "OTP verified successfully for email={}",
                request.email()
        );

        return new OtpResponse(
                true,
                "OTP verified successfully"
        );
    }

    @Transactional
    public OtpResponse resendOtp(SendOtpRequest request, OtpPurpose purpose) {

        otpRepo.deleteByEmailAndPurpose(
                request.email(),
                purpose
        );

        return sendOtp(request, purpose);
    }

    @Transactional
    public void deleteOtp(String email, OtpPurpose purpose) {

        otpRepo.deleteByEmailAndPurpose(
                email,
                purpose
        );
    }

    private boolean isOtpExpired(OtpVerification otpVerification) {

        return otpVerification
                .getExpiresAt()
                .isBefore(LocalDateTime.now());
    }

    private void increaseAttempt(OtpVerification otpVerification) {

        otpVerification.setAttemps(
                otpVerification.getAttemps() + 1
        );

        otpRepo.save(otpVerification);
    }

}
