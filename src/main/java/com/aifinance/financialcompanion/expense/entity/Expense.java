package com.aifinance.financialcompanion.expense.entity;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="expenses" ,
        indexes = {
                @Index(name = "idx_expense_user" , columnList = "user_id"),
                @Index(name = "idx_expense_category" , columnList = "category_id"),
                @Index(name = "idx_expense_date" , columnList = "expense_date")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 150)
    private  String title;

   @Column(nullable = false , precision = 19 , scale = 2)
    private BigDecimal amount;

   @Column(length = 1000)
    private String description;

   @Column(name = "expense_date" , nullable = false)
    private LocalDate expenseDate;

   @Column(name = "created_at" , nullable = false , updatable = false)
    private LocalDateTime createdAt;

   @Column(name = "updated_at" , nullable = false)
    private LocalDateTime updatedAt;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id" , nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    public Expense(String title,  BigDecimal amount, String description,  LocalDate expenseDate, Category category, User user) {
        this.title = title;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.category = category;
        this.user = user;
    }

    @PrePersist
    protected  void onCreate(){
       LocalDateTime now = LocalDateTime.now();
       this.createdAt = now;
       this.updatedAt = now;
   }

   @PreUpdate
    protected void onUpdate(){
       this.updatedAt = LocalDateTime.now();
   }
}
