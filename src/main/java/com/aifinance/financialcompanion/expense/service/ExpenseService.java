package com.aifinance.financialcompanion.expense.service;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.category.repo.CategoryRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.CategoryNotFoundException;
import com.aifinance.financialcompanion.exceptions.ExpenseNotFoundException;
import com.aifinance.financialcompanion.expense.dto.CreateExpenseRequest;
import com.aifinance.financialcompanion.expense.dto.ExpenseResponse;
import com.aifinance.financialcompanion.expense.dto.UpdateExpenseRequest;
import com.aifinance.financialcompanion.expense.entity.Expense;
import com.aifinance.financialcompanion.expense.repo.ExpenseRepository;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final UserRepo userRepo;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

 @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, CustomUserDetails currentUser) {
        Long userId = currentUser.getUserId();

        log.info("Creating expense for userId = {} , title = {} , categoryId = {}", userId, request.title(), request.categoryId());

        User user = getAuthenticatedUser(currentUser);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category is not found"));

        if (isCategoryAllowedForUser(category, userId)) {
            log.warn("Category ownership validation failed for userId={}, categoryId={}", userId, request.categoryId());

            throw new AccessDeniedException("Category is not allowed for this user");
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
                    return new ExpenseNotFoundException("Expense not found");
                });
        log.info("Expense fetched successfully userId ={} , expenseId = {} " , user.getId(), expenseId);
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long expenseId , UpdateExpenseRequest request , CustomUserDetails currentUser){

     User user = getAuthenticatedUser(currentUser);
     log.info("Fetching : expense for updating userId = {},expenseId = {},categoryId ={}",user.getId(),expenseId,request.categoryId());

     Category category = categoryRepository.findById(request.categoryId())
             .orElseThrow(()->{log.warn("Category not found  for UserId = {},expenseId = {}",user.getId(),expenseId);
                 return new AccessDeniedException("Category not found ");
             });

    if(isCategoryAllowedForUser(category, user.getId())){
        log.warn("Category ownership validation failure for userId ={},expenseId ={},categoryId ={}", user.getId(),expenseId ,category.getId());
        throw new AccessDeniedException("Category not belong to this user");
    }
       Expense expense = getExpenseForCurrentUser(expenseId,currentUser);
       expense.setTitle(request.title());
       expense.setAmount(request.amount());
       expense.setDescription(request.description());
       expense.setExpenseDate(request.expenseDate());
       expense.setCategory(category);

       Expense updateExpense = expenseRepository.save(expense);
       log.info("Expense updated successfull for userId ={} , expenseId = {}, categoryId = {}",user.getId(),expenseId,category.getId());

       return mapToResponse(updateExpense);

    }

    @Transactional
    public void deleteExpense(Long expenseId , CustomUserDetails currentUser){

     User user = getAuthenticatedUser(currentUser);
     log.info("Deleting expense for userId = {},expenseId ={}",user.getId(),expenseId);

     Expense expense = getExpenseForCurrentUser(expenseId,currentUser);

      expenseRepository.delete(expense);
      log.info("expense deleted successfully for  userId = {},expenseId = {}  ", user.getId(),expenseId);
    }

    private User  getAuthenticatedUser(CustomUserDetails currentUser) {

        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new AccessDeniedException("user is not found"));
    }

    private boolean isCategoryAllowedForUser(Category category , Long userId){
        if(category.isPredefined()){
            return false;
        }
        User categoryOwner = category.getUser();
        return categoryOwner == null || !categoryOwner.getId().equals(userId);
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

    public Expense getExpenseForCurrentUser(Long expenseId , CustomUserDetails currentUser){
     User user = getAuthenticatedUser(currentUser);

     return expenseRepository.findByIdAndUser(expenseId, user)
             .orElseThrow(()->new ExpenseNotFoundException("expense not found") );
    }


}
