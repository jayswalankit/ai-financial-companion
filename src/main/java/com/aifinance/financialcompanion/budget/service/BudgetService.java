package com.aifinance.financialcompanion.budget.service;

import com.aifinance.financialcompanion.budget.dto.BudgetRequest;
import com.aifinance.financialcompanion.budget.dto.BudgetResponse;
import com.aifinance.financialcompanion.budget.dto.BudgetStatusResponse;
import com.aifinance.financialcompanion.budget.entity.MonthlyBudget;
import com.aifinance.financialcompanion.budget.repo.MonthlyBudgetRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.BudgetNotFoundException;
import com.aifinance.financialcompanion.exceptions.UserNotFound;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.report.service.ReportService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserRepo userRepo;
    private final ReportService reportService;

    @Transactional
    public BudgetResponse createOrUpdateBudget(BudgetRequest request, CustomUserDetails currentUser){

        User user = getAuthenticated(currentUser);
        log.info("Processing budget for userId = {}",user.getId());

        MonthlyBudget budget;
        MonthlyBudget existingBudget = monthlyBudgetRepository.findByUserIdAndMonthAndYear(user.getId(),request.month(),request.year());

        if( existingBudget != null){
            budget = existingBudget;
            budget.setBudgetAmount(request.budgetAmount());
        }
        else{
            budget = new MonthlyBudget(request.month(),
                    request.year(),
                    request.budgetAmount(),
                    user);

            log.info(" Updating budget for userId={}, month={}, year={}," +
                    "            user.getId()," +
                    "            request.month()," +
                    "            request.year()",user.getId(),request.month(),
                    request.year());
        }

        MonthlyBudget savedBudget = monthlyBudgetRepository.save(budget);

        return mapToResponse(savedBudget);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(CustomUserDetails currentUser, Integer month,Integer year){

        User user = getAuthenticated(currentUser);
        log.info("Getting budget of userId = {}",user.getId());

        MonthlyBudget budget = monthlyBudgetRepository.findByUserIdAndMonthAndYear(user.getId(),month,year);

        if(budget == null){
            throw new BudgetNotFoundException("Budget not found exception");
        }
         return mapToResponse(budget);
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse budgetStatus(CustomUserDetails currentUser) {

        User user = getAuthenticated(currentUser);
        log.info("Getting budget status for userId = {}", user.getId());

        YearMonth currentMonth = YearMonth.now();
        Integer month = currentMonth.getMonthValue();
        Integer year = currentMonth.getYear();

        MonthlyBudget currentBudget = monthlyBudgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year);
        boolean budgetExist;

        if (currentBudget != null) {
            return new BudgetStatusResponse(true,
                    currentBudget.getBudgetAmount(),
                    currentBudget.getMonth(),
                    currentBudget.getYear());
        }

        MonthlyBudget latestBudget = monthlyBudgetRepository.findTopByUserIdOrderByYearDescMonthDesc(user.getId());

        if (latestBudget != null) {
            return new BudgetStatusResponse(false,
                    latestBudget.getBudgetAmount(),
                    latestBudget.getMonth(),
                    latestBudget.getYear());
        }

        return new BudgetStatusResponse(false,
                null,
                null,
                null);

    }

    private User getAuthenticated(CustomUserDetails currentUser){

        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()-> new UserNotFound("Authenticated User not found"));
    }

    private BudgetResponse  mapToResponse(MonthlyBudget budget){
        return new BudgetResponse(budget.getId(),
                budget.getMonth(),
                budget.getYear(),
                budget.getBudgetAmount(),
                budget.getCreatedAt(),
                budget.getUpdatedAt());
    }

    public BigDecimal getCurrentMonthBudget(User user) {

        YearMonth currentMonth = YearMonth.now();

        Integer month = currentMonth.getMonthValue();
        Integer year = currentMonth.getYear();


        MonthlyBudget budget =
                monthlyBudgetRepository.findByUserIdAndMonthAndYear(
                        user.getId(),
                        month,
                        year
                );

        if (budget == null) {
            return BigDecimal.ZERO;
        }

        return budget.getBudgetAmount();
    }
}
