package com.aifinance.financialcompanion.expense.controller;

import com.aifinance.financialcompanion.expense.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class ExpenseControllerTest {

    private final ExpenseController expenseController = new ExpenseController(mock(ExpenseService.class));

    @Test
    void buildPageableParsesCommaSeparatedSortToken() {
        Pageable pageable = invokeBuildPageable(new String[]{"expenseDate,desc"});

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Order.desc("expenseDate")), pageable.getSort());
    }

    @Test
    void buildPageableIgnoresStandaloneDirectionTokens() {
        Pageable pageable = invokeBuildPageable(new String[]{"expenseDate", "desc", "createdAt", "desc"});

        assertEquals(
                Sort.by(Sort.Order.desc("expenseDate"), Sort.Order.desc("createdAt")),
                pageable.getSort()
        );
    }

    @Test
    void buildPageableFallsBackToUnsortedWhenOnlyDirectionsAreProvided() {
        Pageable pageable = invokeBuildPageable(new String[]{"desc"});

        assertFalse(pageable.getSort().isSorted());
    }

    private Pageable invokeBuildPageable(String[] sortValues) {
        return ReflectionTestUtils.invokeMethod(
                expenseController,
                "buildPageable",
                0,
                10,
                sortValues
        );
    }
}
