package com.aifinance.financialcompanion.preference.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.preference.dto.UserPreferenceResponse;
import com.aifinance.financialcompanion.preference.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepo extends JpaRepository<UserPreference,Long> {

    UserPreference findByUser(User user);

}
