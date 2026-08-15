package com.aifinance.financialcompanion.auth.repo;

import com.aifinance.financialcompanion.auth.entity.PendingSignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingSignupRepository extends JpaRepository<PendingSignup, Long> {
    Optional<PendingSignup> findByEmail(String email);

    void deleteByEmail(String email);
}
