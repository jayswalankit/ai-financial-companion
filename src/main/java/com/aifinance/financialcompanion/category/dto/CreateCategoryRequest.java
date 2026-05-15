package com.aifinance.financialcompanion.category.dto;

import com.aifinance.financialcompanion.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(

        @NotBlank(message = "Name is required")
        String name ,

        @NotNull(message = "Type is required")
        CategoryType type
) {
}
