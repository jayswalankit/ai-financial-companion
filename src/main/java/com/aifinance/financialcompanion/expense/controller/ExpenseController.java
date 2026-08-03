package com.aifinance.financialcompanion.expense.controller;

import com.aifinance.financialcompanion.expense.dto.CreateExpenseRequest;
import com.aifinance.financialcompanion.expense.dto.ExpenseResponse;
import com.aifinance.financialcompanion.expense.dto.UpdateExpenseRequest;
import com.aifinance.financialcompanion.expense.service.ExpenseService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public ResponseEntity<Page<ExpenseResponse>> getAllExpenses(@AuthenticationPrincipal CustomUserDetails currentUser ,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Long categoryId,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false) String[] sort){

        Pageable pageable = buildPageable(page,size,sort);
        Page<ExpenseResponse> responses = expenseService.getAllExpenses(currentUser, keyword,categoryId,startDate,endDate,pageable);
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

    private Pageable buildPageable(int page,int size,String[] sortValues){
        if(sortValues == null || sortValues.length == 0){
            return PageRequest.of(page,size);
        }

        List<Sort.Order> orders = new ArrayList<>();
        List<String> tokens = Arrays.stream(sortValues)
                .filter(value -> value != null && !value.isBlank())
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        for (int index = 0; index < tokens.size(); ) {
            String property = tokens.get(index);

            if (isDirectionToken(property)) {
                index++;
                continue;
            }

            Sort.Direction direction = Sort.Direction.DESC;
            if (index + 1 < tokens.size() && isDirectionToken(tokens.get(index + 1))) {
                direction = Sort.Direction.fromString(tokens.get(index + 1));
                index += 2;
            } else {
                index++;
            }

            orders.add(new Sort.Order(direction, property));
        }

        if(orders.isEmpty()){

            return PageRequest.of(page,size);
        }

        return PageRequest.of(page,size,Sort.by(orders));
    }

    private boolean isDirectionToken(String token) {
        return "asc".equalsIgnoreCase(token) || "desc".equalsIgnoreCase(token);
    }

}
