package com.aifinance.financialcompanion.expense.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense , Long> {

    Page<Expense> findByUser(User user , Pageable pageable);

    Optional<Expense> findByIdAndUser(Long id , User user);
}
