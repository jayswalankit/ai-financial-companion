package com.aifinance.financialcompanion.expense.service;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.category.repo.CategoryRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.CategoryNotFoundException;
import com.aifinance.financialcompanion.exceptions.ExpenseNotFoundException;
import com.aifinance.financialcompanion.exceptions.InvalidDateRangeException;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    public Page<ExpenseResponse> getAllExpenses (CustomUserDetails currentUser ,
                                                 String keyword,
                                                  Long categoryId,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  Pageable pageable){

        User user = getAuthenticatedUser(currentUser);

        validateDateRange(startDate,endDate);
        String normalizedKeyword = normalizedKeyword(keyword);
        Pageable resolvedPageable = resolvePageable(pageable);

        log.info("Fetching expenses for UserId = {} , page = {} , size ={} , sort = {}"
                , user.getId()
                , pageable.getPageNumber()
                , pageable.getPageSize()
                , pageable.getSort());

        log.info("Fetched expenses with filters for  userID = {} , keyword='{}', categoryId={}, startDate={}, endDate={}, page={}, size={}, sort={}",
        user.getId(),
        normalizedKeyword,
        categoryId,
        startDate,
        endDate,
        resolvedPageable.getPageNumber(),
        resolvedPageable.getPageSize(),
        resolvedPageable.getSort());

          Page<Expense> expensePage = findExpensesByFilters(
                  user,
                  normalizedKeyword,
                  categoryId,
                  startDate,
                  endDate,
                  resolvedPageable
          );

          Page<ExpenseResponse> expensesPage = expensePage
                  .map(this::mapToResponse);

        log.info(
                "Fetched expenses for userId={}, returnedItems={}, totalItems={}",
                user.getId(),
                expensesPage.getNumberOfElements(),
                expensesPage.getTotalElements()
        );
        return expensesPage;
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId , CustomUserDetails currentUser) {
        User user = getAuthenticatedUser(currentUser);

        log.info("Fetching expense by expense id  userId = {} , expenseId = {}", user.getId(), expenseId);

        Expense expense = getExpenseForCurrentUser(expenseId,currentUser);
        log.info("Expense fetched successfully userId ={} , expenseId = {} " , user.getId(), expenseId);
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long expenseId , UpdateExpenseRequest request , CustomUserDetails currentUser){

     User user = getAuthenticatedUser(currentUser);
     log.info("Fetching : expense for updating userId = {},expenseId = {},categoryId ={}",user.getId(),expenseId,request.categoryId());

     Category category = categoryRepository.findById(request.categoryId())
             .orElseThrow(()->{log.warn("Category not found  for UserId = {},expenseId = {}",user.getId(),expenseId);
                 return new CategoryNotFoundException("Category not found ");
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
                .orElseThrow(()->new UserNotFound("user is not found"));
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

    private Page <Expense> findExpensesByFilters(User user,
                                                 String keyword,
                                                 Long categoryId,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Pageable pageable){

     boolean hasKeyword = keyword != null;
     boolean hasCategory = categoryId != null;
     boolean hasDateRange = startDate != null && endDate != null;

     if(hasCategory && hasKeyword && hasDateRange){
         return expenseRepository.findByUserAndCategoryIdAndTitleContainingIgnoreCaseAndExpenseDateBetween(
                 user,
                 categoryId,
                 keyword,
                 startDate,
                 endDate,
                 pageable
         );
     }

     if (hasCategory && hasKeyword){
         return expenseRepository.findByUserAndCategoryIdAndTitleContainingIgnoreCase(
                 user,
                 categoryId,
                 keyword,
                 pageable
         );
     }

     if(hasCategory && hasDateRange){
         return expenseRepository.findByUserAndCategoryIdAndExpenseDateBetween(
                 user,
                 categoryId,
                 startDate,
                 endDate,
                 pageable);
     }

     if(hasKeyword && hasDateRange){
         return expenseRepository.findByUserAndTitleContainingIgnoreCaseAndExpenseDateBetween(
                 user,
                 keyword,
                 startDate,
                 endDate,
                 pageable
         );
     }

     if(hasCategory){
         return expenseRepository.findByUserAndCategoryId(user,
                 categoryId,
                 pageable);
     }

     if(hasKeyword){
         return expenseRepository.findByUserAndTitleContainingIgnoreCase(user,
                 keyword,
                 pageable);
     }

     if(hasDateRange){
         return expenseRepository.findByUserAndExpenseDateBetween(user,
                 startDate,
                 endDate,
                 pageable);
     }

     return expenseRepository.findByUser(user,pageable);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate){

     if((startDate == null && endDate != null) || (startDate != null && endDate == null)){
         throw new IllegalArgumentException("Both start and end date are required for Date Range");
     }

     if(startDate != null && startDate.isAfter(endDate)){
         throw new InvalidDateRangeException("Start date cannot after endDate");
     }
    }

    private String normalizedKeyword(String keyword){
     if(keyword == null){
         return null;
     }

     String normalizedKeyword = keyword.trim();
     return normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }

    private Pageable resolvePageable(Pageable pageable){

        Sort defaultSort = Sort.by(
                Sort.Order.desc("expenseDate"),
                Sort.Order.desc("createdAt")
        );

        Sort requestSort = pageable.getSort();
        if(requestSort == null || requestSort.isUnsorted()){
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),defaultSort);
        }

        Sort resolvedSort = requestSort.getOrderFor("createdAt") == null?
                requestSort.and(Sort.by(Sort.Order.desc("createdAt")))
                :requestSort;

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),resolvedSort);
    }
}
