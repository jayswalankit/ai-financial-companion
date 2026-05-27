package com.aifinance.financialcompanion.report.repo;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.expense.entity.Expense;
import com.aifinance.financialcompanion.report.projection.CategoryExpenseProjection;
import com.aifinance.financialcompanion.report.projection.CategoryGrowthProjection;
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
public interface ReportRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user = :user
            """)
    BigDecimal getTotalExpenseByUser(
            @Param("user") User user
    );

    @Query("""
            SELECT COUNT(e)
            FROM Expense e
            WHERE e.user = :user
            """)
    Long getTotalTransactionCountByUser(
            @Param("user") User user
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user = :user
            AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getTotalExpenseByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT e.category.name AS categoryName,
                   COALESCE(SUM(e.amount),0) AS totalAmount
            FROM Expense e
            WHERE e.user = :user
            GROUP BY e.category.name
            ORDER BY totalAmount DESC
            """)
    List<CategoryExpenseProjection> findTopCategoriesByUser(
            @Param("user") User user,
            PageRequest pageRequest
    );

    @Query("""
            SELECT FUNCTION('date_format', e.expenseDate, '%x-W%v') AS weekLabel,
                   COALESCE(SUM(e.amount), 0) AS totalExpense
            FROM Expense e
            WHERE e.user = :user
            AND e.expenseDate BETWEEN :startDate AND :endDate
            GROUP BY FUNCTION('date_format', e.expenseDate, '%x-W%v')
            ORDER BY FUNCTION('date_format', e.expenseDate, '%x-W%v') ASC
            """)
    List<WeeklyExpenseProjection> getWeeklyTrendByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT c.name AS categoryName,
                   COALESCE(SUM(e.amount),0) AS totalAmount
            FROM Expense e
            JOIN e.category c
            WHERE e.user = :user
            AND e.expenseDate BETWEEN :startDate AND :endDate
            GROUP BY c.name
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategoryExpenseProjection> findTopCategoriesByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            PageRequest pageRequest
    );

    @Query("""
            SELECT COALESCE(MAX(e.amount),0)
            FROM Expense e
            WHERE e.user = :user
            AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getHighestExpenseByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT e
            FROM Expense e
            WHERE e.user = :user
            AND e.expenseDate BETWEEN :startDate AND :endDate
            AND e.amount = (
                SELECT MAX(innerExpense.amount)
                FROM Expense innerExpense
                WHERE innerExpense.user = :user
                AND innerExpense.expenseDate BETWEEN :startDate AND :endDate
            )
            AND e.id = (
                SELECT MAX(tieBreakerExpense.id)
                FROM Expense tieBreakerExpense
                WHERE tieBreakerExpense.user = :user
                AND tieBreakerExpense.expenseDate BETWEEN :startDate AND :endDate
                AND tieBreakerExpense.amount = (
                    SELECT MAX(maxExpense.amount)
                    FROM Expense maxExpense
                    WHERE maxExpense.user = :user
                    AND maxExpense.expenseDate BETWEEN :startDate AND :endDate
                )
            )
            """)
    Expense findTopExpenseByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT c.name AS categoryName,
                   COALESCE(SUM(CASE
                       WHEN e.expenseDate BETWEEN :currentMonthStart AND :currentMonthEnd
                       THEN e.amount
                       ELSE 0
                   END), 0) AS currentMonthAmount,

                   COALESCE(SUM(CASE
                       WHEN e.expenseDate BETWEEN :previousMonthStart AND :previousMonthEnd
                       THEN e.amount
                       ELSE 0
                   END), 0) AS previousMonthAmount

            FROM Expense e
            JOIN e.category c

            WHERE e.user = :user
            AND e.expenseDate BETWEEN :previousMonthStart AND :currentMonthEnd

            GROUP BY c.name

            ORDER BY COALESCE(SUM(CASE
                       WHEN e.expenseDate BETWEEN :currentMonthStart AND :currentMonthEnd
                       THEN e.amount
                       ELSE 0
                   END), 0) DESC
            """)
    List<CategoryGrowthProjection> getCategoryGrowthComparison(
            @Param("user") User user,
            @Param("currentMonthStart") LocalDate currentMonthStart,
            @Param("currentMonthEnd") LocalDate currentMonthEnd,
            @Param("previousMonthStart") LocalDate previousMonthStart,
            @Param("previousMonthEnd") LocalDate previousMonthEnd
    );
}