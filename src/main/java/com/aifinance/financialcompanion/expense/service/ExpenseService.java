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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        User user = getAuthenticatedUser(currentUser);

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

    private User  getAuthenticatedUser(CustomUserDetails currentUser) {

        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new IllegalArgumentException("user is not found"));
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

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAllExpenses (CustomUserDetails currentUser , Pageable pageable){

        User user = getAuthenticatedUser(currentUser);

        log.info("Fetching expenses for UserId = {} , page = {} , size ={} , sort = {}"
        , user.getId()
        , pageable.getPageNumber()
        , pageable.getPageSize()
        , pageable.getSort());

        Page<ExpenseResponse> expensesPage = expenseRepository.findByUser(user , pageable)
                .map(this::mapToResponse);

        log.info("Fetched response for userID = {} , returnItems = {} , totalItems = {}"
        ,user.getId()
        ,expensesPage.getNumberOfElements()
        ,expensesPage.getTotalElements());

        return expensesPage;
    }

    public ExpenseResponse getExpenseById(Long expenseId , CustomUserDetails currentUser) {
        User user = getAuthenticatedUser(currentUser);

        log.info("Fetching expense by expense id  userId = {} , expenseId = {}", user.getId(), expenseId);

        Expense expense = expenseRepository.findByIdAndUser(expenseId, user).
                orElseThrow(() -> {
                    log.warn("Expense not found or not owned userId = {} , expenseId ={}", user.getId(), expenseId);
                    return new IllegalArgumentException("Expense not found");
                });
        log.info("Expense fetched successfully userId ={} , expenseId = {} " , user.getId(), expenseId);
        return mapToResponse(expense);
    }
}
