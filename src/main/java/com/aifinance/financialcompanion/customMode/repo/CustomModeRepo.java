package com.aifinance.financialcompanion.customMode.repo;

import com.aifinance.financialcompanion.customMode.dto.CustomModeResponse;
import com.aifinance.financialcompanion.customMode.entity.CustomMode;
import com.aifinance.financialcompanion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomModeRepo extends JpaRepository<CustomMode,Long> {
    List<CustomMode> findByUser(User user);

    Optional<CustomMode> findByIdAndUser(Long modeId, User user);

    boolean existsByUserAndModeName(User user, String s);
}
