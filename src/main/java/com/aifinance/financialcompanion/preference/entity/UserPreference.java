package com.aifinance.financialcompanion.preference.entity;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import com.aifinance.financialcompanion.enums.UserMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Getter
@Setter
@Table(name = "user_preferences")
@Entity
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private  Long id;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserMode userMode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationMode notificationMode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false ,updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",nullable = false)
   private  LocalDateTime updatedAt;

}
