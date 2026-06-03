package com.aifinance.financialcompanion.budget.repo;

import com.aifinance.financialcompanion.budget.entity.MonthlyBudget;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget,Long> {
  MonthlyBudget findByUserIdAndMonthAndYear(Long id,  Integer month, Integer year);

    MonthlyBudget findTopByUserIdOrderByYearDescMonthDesc(Long id);
}
