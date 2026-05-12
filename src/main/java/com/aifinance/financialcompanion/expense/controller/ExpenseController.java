package com.aifinance.financialcompanion.expense.controller;

import com.aifinance.financialcompanion.expense.dto.CreateExpenseRequest;
import com.aifinance.financialcompanion.expense.dto.ExpenseResponse;
import com.aifinance.financialcompanion.expense.dto.UpdateExpenseRequest;
import com.aifinance.financialcompanion.expense.service.ExpenseService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping()
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody @Valid CreateExpenseRequest request , @AuthenticationPrincipal CustomUserDetails currentUser){

        ExpenseResponse response = expenseService.createExpense(request , currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getAllExpenses(@AuthenticationPrincipal CustomUserDetails currentUser , @PageableDefault(sort = "expenseDate")Pageable pageable){

        Page<ExpenseResponse> responses = expenseService.getAllExpenses(currentUser , pageable);
        return ResponseEntity.ok(responses);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpensesById(@PathVariable Long id , @AuthenticationPrincipal CustomUserDetails currentUser){

        ExpenseResponse response = expenseService.getExpenseById(id, currentUser);
        return  ResponseEntity.ok(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id , @RequestBody@Valid UpdateExpenseRequest request , @AuthenticationPrincipal CustomUserDetails currentUser){

        return  ResponseEntity.ok(expenseService.updateExpense(id,request,currentUser));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteExpense(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser){
        expenseService.deleteExpense(id,currentUser);
        return ResponseEntity.noContent().build();
    }

}
