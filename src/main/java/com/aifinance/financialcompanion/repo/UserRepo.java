package com.aifinance.financialcompanion.repo;

import com.aifinance.financialcompanion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User>findByEmail(String mail);
}
