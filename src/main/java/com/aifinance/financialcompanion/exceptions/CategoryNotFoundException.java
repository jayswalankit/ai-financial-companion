package com.aifinance.financialcompanion.exceptions;

public class CategoryNotFoundException extends RuntimeException {

    private final Long categoryId;

    public CategoryNotFoundException(Long categoryId)
    {
        super("Category with id = "+ categoryId + "not found");
        this.categoryId = categoryId;
    }

    public CategoryNotFoundException(String message) {
        super(message);
        this.categoryId = null;
    }

    public Long getCategoryId() {
        return categoryId;
    }
}
