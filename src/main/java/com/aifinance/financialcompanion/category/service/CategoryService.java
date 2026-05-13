package com.aifinance.financialcompanion.category.service;

import com.aifinance.financialcompanion.category.dto.CategoryResponse;
import com.aifinance.financialcompanion.category.dto.CreateCategoryRequest;
import com.aifinance.financialcompanion.category.dto.UpdateCategoryRequest;
import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.category.repo.CategoryRepository;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.exceptions.CategoryNotFoundException;
import com.aifinance.financialcompanion.expense.repo.ExpenseRepository;
import com.aifinance.financialcompanion.repo.UserRepo;
import com.aifinance.financialcompanion.security.userDetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor()
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepo  userRepo;
    private final ExpenseRepository expenseRepository;

    @Transactional
            public  CategoryResponse createCategory(CreateCategoryRequest request, CustomUserDetails currentUser) {

        User user = getAuthenticatedUser(currentUser);
        String normalizedName  = request.name().trim();
        log.info("Creating category for userId = {},CategoryName = {}, CategoryType = {}", user.getId(), request.name(), request.type());

       if(categoryRepository.existsByPredefinedTrueAndNameIgnoreCase(normalizedName)){
           log.warn("Category cannot created due to the predefined name conflict for userId = {}, categoryName = {}",user.getId(),normalizedName);
           throw  new IllegalArgumentException("Category name is already present in predefined  category");
       }

       if(categoryRepository.existsByUserAndNameIgnoreCase(user,normalizedName)){
           log.warn("Category cannot be created due to duplicate userCategory for userId ={}, categoryName = {}",user.getId(),normalizedName);
           throw new IllegalArgumentException("Category name is already present is userCategories  List");
       }

       Category category = new Category(
               normalizedName,
               request.type(),
               false,
               user
       );

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully for userId = {}, categoryId = {}",user.getId(), savedCategory.getId());

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getType(),
                savedCategory.isPredefined()
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories (CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);
              log.info("Fetching categories for userId = {}",user.getId());

              List<CategoryResponse> categories = categoryRepository.findByPredefinedTrueOrUser(user)
                      .stream()
                      .map(this::mapToResponse)
                      .toList();
              log.info("Fetched categories for userId = {},categorySize = {}",user.getId(),categories.size());

              return  categories;
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request , CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);
        String normalizedName = request.name().trim();
        log.info("Updating category for userId = {} , categoryId = {}",user.getId(),categoryId);

        Category category = getUserCategory(categoryId,currentUser);

        if(categoryRepository.existsByPredefinedTrueAndNameIgnoreCase(normalizedName)){
            log.warn("category updated block due to predefined name conflict for userId = {}, categoryId = {}",user.getId(),categoryId);
            throw new AccessDeniedException("Category name already exist in predefined category");
        }

        if(categoryRepository.existsByUserAndNameIgnoreCaseAndIdNot(user,normalizedName,categoryId)){
           log.warn("category updated block due to category name already present for userId = {},categoryId = {}",user.getId(),categoryId);
            throw new IllegalArgumentException(
                    "Category name already exists"
            );
        }
          category.setName(normalizedName);

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully. userId={}, categoryId={}", user.getId(), updatedCategory.getId());
        return mapToResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId, CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);
        log.info("Deleting category for userId = {} , categoryId = {}",user.getId(),categoryId);

        Category category = getUserCategory(categoryId,currentUser);

        if(expenseRepository.existsByCategory(category)){
            log.warn("Category deletion blocked because expenses exist. userId={}, categoryId={}",
                    user.getId(),
                    categoryId);

            throw new IllegalArgumentException(
                    "Category cannot be deleted because expenses exist for this category"
            );
        }

        categoryRepository.delete(category);
    }

    public User getAuthenticatedUser(CustomUserDetails currentUser){

        return userRepo.findById(currentUser.getUserId())
                .orElseThrow(()->new UsernameNotFoundException("Authenticated user not found"));
    }

    public CategoryResponse mapToResponse(Category category){
        return  new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isPredefined()

        );

    }

    public Category getUserCategory(Long categoryId , CustomUserDetails currentUser){

        User user = getAuthenticatedUser(currentUser);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new CategoryNotFoundException("Category not found"));

        if(category.isPredefined()){
            throw new AccessDeniedException("Predefined categories cannot be modified");
        }

        User owner = category.getUser();

        if(owner == null || !owner.getId().equals(user.getId())){
             throw new AccessDeniedException("Category is not allowed to this user ");
        }

        return category;
    }
}
