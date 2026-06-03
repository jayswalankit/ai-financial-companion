package com.aifinance.financialcompanion.budget.controller;

import com.aifinance.financialcompanion.budget.dto.BudgetRequest;
import com.aifinance.financialcompanion.budget.dto.BudgetResponse;
import com.aifinance.financialcompanion.budget.dto.BudgetStatusResponse;
import com.aifinance.financialcompanion.budget.service.BudgetService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/budget")
@RequiredArgsConstructor

public class BudgetController {

    private final  BudgetService budgetService;

  @PostMapping
    public ResponseEntity<BudgetResponse> createOrUpdateBudget(@Valid @RequestBody BudgetRequest request, @AuthenticationPrincipal CustomUserDetails currentUser){

      BudgetResponse budget = budgetService.createOrUpdateBudget(request,currentUser);
      return ResponseEntity.ok(budget);

  }

  @GetMapping
    public ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal CustomUserDetails currentUser,@RequestParam(required = true) @Min(1)   @Max(12) Integer month, @RequestParam(required = true) @Positive Integer year){

      BudgetResponse budget = budgetService.getBudget(currentUser,month,year);
      return ResponseEntity.ok(budget);
  }

  @GetMapping("/status")
  public ResponseEntity<BudgetStatusResponse>getBudgetStatus(@AuthenticationPrincipal CustomUserDetails currentUSer){

      BudgetStatusResponse response = budgetService.budgetStatus(currentUSer);
      return ResponseEntity.ok(response);

  }
}
