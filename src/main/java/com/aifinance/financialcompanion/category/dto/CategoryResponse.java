package com.aifinance.financialcompanion.category.dto;

import com.aifinance.financialcompanion.category.entity.CategoryType;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type,
        Boolean predefined
) {
}
