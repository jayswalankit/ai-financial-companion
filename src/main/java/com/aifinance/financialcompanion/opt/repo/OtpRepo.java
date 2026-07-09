package com.aifinance.financialcompanion.opt.repo;

import com.aifinance.financialcompanion.enums.OtpPurpose;
import com.aifinance.financialcompanion.opt.entity.OtpVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepo extends JpaRepository<OtpVerification,Long> {
    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);

    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(@NotBlank(message = "Email is required") @Email(message = "Invvalid email") String email, @NotNull(message = "Purpose is required") OtpPurpose purpose);
}
