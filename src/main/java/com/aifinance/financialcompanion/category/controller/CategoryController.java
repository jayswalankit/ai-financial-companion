package com.aifinance.financialcompanion.category.controller;

import com.aifinance.financialcompanion.category.dto.CategoryResponse;
import com.aifinance.financialcompanion.category.dto.CreateCategoryRequest;
import com.aifinance.financialcompanion.category.dto.UpdateCategoryRequest;
import com.aifinance.financialcompanion.category.service.CategoryService;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request, @AuthenticationPrincipal CustomUserDetails currentUser){

        CategoryResponse response = categoryService.createCategory(request ,currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@AuthenticationPrincipal CustomUserDetails currentUser){

        List<CategoryResponse> response = categoryService.getCategories(currentUser);
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request , @AuthenticationPrincipal CustomUserDetails currentUser){

        CategoryResponse response = categoryService.updateCategory(id,request,currentUser);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id , @AuthenticationPrincipal CustomUserDetails currentUser){

        categoryService.deleteCategory(id,currentUser);
        return ResponseEntity.noContent().build();
    }

}
