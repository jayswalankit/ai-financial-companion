package com.aifinance.financialcompanion.auth.repo;

import com.aifinance.financialcompanion.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User>findByEmail(String mail);
}
