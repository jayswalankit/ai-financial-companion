package com.aifinance.financialcompanion.expense.repo;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense , Long> {

    Page<Expense> findByUser(User user , Pageable pageable);

    Optional<Expense> findByIdAndUser(Long id , User user);

    boolean existsByCategory(Category category);

    Page<Expense> findByUserAndCategoryIdAndTitleContainingIgnoreCaseAndExpenseDateBetween(User user, Long categoryId, String title, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Expense> findByUserAndCategoryIdAndTitleContainingIgnoreCase(User user, Long categoryId, String keyword, Pageable pageable);

    Page<Expense> findByUserAndCategoryIdAndExpenseDateBetween(User user, Long categoryId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Expense> findByUserAndTitleContainingIgnoreCaseAndExpenseDateBetween(User user, String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Expense> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    Page<Expense> findByUserAndTitleContainingIgnoreCase(User user, String keyword, Pageable pageable);

    Page<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
