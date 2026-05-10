package com.aifinance.financialcompanion.expense.service;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.category.repo.CategoryRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.expense.dto.CreateExpenseRequest;
import com.aifinance.financialcompanion.expense.dto.ExpenseResponse;
import com.aifinance.financialcompanion.expense.entity.Expense;
import com.aifinance.financialcompanion.expense.repo.ExpenseRepository;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final UserRepo userRepo;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;


    public ExpenseResponse createExpense(CreateExpenseRequest request, CustomUserDetails currentUser) {
        Long userId = currentUser.getUserId();

        log.info("Creating expense for userId = {} , title = {} , categoryId = {}", userId, request.title(), request.categoryId());

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category is not found"));

        if (!isCategoryAllowedForUser(category, userId)) {
            log.warn("Category ownership validation failed for userId={}, categoryId={}", userId, request.categoryId());

            throw new IllegalArgumentException("Category is not allowed for this user");
        }
        Expense expense = new Expense(
                request.title().trim(),
                request.amount(),
                request.description(),
                request.expenseDate(),
                category,
                user
        );

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created successfully. expenseId = {} , userId = {}", savedExpense.getId(), userId);

        return mapToResponse(savedExpense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getCategory().getName(),
                expense.getCreatedAt()
        );
    }

    private boolean isCategoryAllowedForUser(Category category , Long userId){
        if(category.isPredefined()){
            return true;
        }
        User categoryOwner = category.getUser();
        return categoryOwner != null && categoryOwner.getId().equals(userId);
    }


}
