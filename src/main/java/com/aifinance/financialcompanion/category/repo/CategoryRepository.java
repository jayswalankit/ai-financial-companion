package com.aifinance.financialcompanion.category.repo;

import com.aifinance.financialcompanion.category.entity.Category;
import com.aifinance.financialcompanion.category.entity.CategoryType;
import com.aifinance.financialcompanion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category , Long> {

//    List<Category> findByUser(User user);
//
//    List<Category> findByPredefinedTrue();
//
    List<Category> findByPredefinedTrueOrUser(User user);
//
//    List<Category> findByUserAndType(User user , CategoryType type);

    boolean existsByPredefinedTrueAndNameIgnoreCase(String normalizedName);

    boolean existsByUserAndNameIgnoreCase(User user, String normalizedName);

    boolean existsByUserAndNameIgnoreCaseAndIdNot(User user, String normalizedName, Long categoryId);
}
