package com.aifinance.financialcompanion.budget.entity;


import com.aifinance.financialcompanion.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "monthly_budget",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id","month","year"})})

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    @Min(1)
    @Max(12)
    Integer month;

    @Column(nullable = false)
    Integer year;

    @Column(nullable = false)
    BigDecimal budgetAmount;

    @Column(nullable = false,updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @JoinColumn(name = "user_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)

    User user;

    public MonthlyBudget(
            Integer month,
            Integer year,
            BigDecimal budgetAmount,
            User user
    ) {
        this.month = month;
        this.year = year;
        this.budgetAmount = budgetAmount;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}
