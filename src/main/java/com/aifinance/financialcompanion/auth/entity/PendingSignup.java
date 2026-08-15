package com.aifinance.financialcompanion.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_signups")
@Getter
@Setter
@NoArgsConstructor
public class PendingSignup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }
}
