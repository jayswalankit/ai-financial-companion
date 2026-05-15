package com.aifinance.financialcompanion.category.entity;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_user", columnList = "user_id"),
                @Index(name = "idx_category_type", columnList = "type"),
                @Index(name = "idx_category_predefined", columnList = "predefined")
        }
)

@RequiredArgsConstructor
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 20)
    private CategoryType type;

    @Column (nullable = false)
    private boolean predefined;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public Category(String name,  CategoryType type, boolean predefined, User user) {
        this.name = name;
        this.type = type;
        this.predefined = predefined;
        this.user = user;
    }
}
