package com.aifinance.financialcompanion.report.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.expense.entity.Expense;
import com.aifinance.financialcompanion.report.dto.CategorySummaryResponse;
import com.aifinance.financialcompanion.report.projection.CategoryExpenseProjection;
import com.aifinance.financialcompanion.report.projection.WeeklyExpenseProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface ReportRepository extends JpaRepository<Expense,Long> {
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal getTotalExpenseByUser(@Param("user") User user);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user = :user")
    Long getTotalTransactionCountByUser(@Param("user") User user);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user = :user
        AND e.expenseDate BETWEEN :startDate AND :endDate
        """)
    BigDecimal getTotalExpenseByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate end );

    @Query("""
       SELECT e.category.name AS categoryName,
              COALESCE(SUM(e.amount),0) AS totalAmount
       FROM Expense e
       WHERE e.user = :user
       GROUP BY e.category.name
       ORDER BY totalAmount DESC
       """)
    List<CategoryExpenseProjection> findTopCategoriesByUser(@Param("user") User user, PageRequest of);

    @Query("""
       SELECT FUNCTION('date_format', e.expenseDate, '%x-W%v') AS weekLabel,
              COALESCE(SUM(e.amount), 0) AS totalExpense
       FROM Expense e
       WHERE e.user = :user
       AND e.expenseDate BETWEEN :startDate AND :endDate
       GROUP BY FUNCTION('date_format', e.expenseDate, '%x-W%v')
       ORDER BY FUNCTION('date_format', e.expenseDate, '%x-W%v') ASC
       """)
    List<WeeklyExpenseProjection> getWeeklyTrendByUserAndDateBetween(@Param("user") User user,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
