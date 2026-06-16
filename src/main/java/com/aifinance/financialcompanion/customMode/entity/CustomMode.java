package com.aifinance.financialcompanion.customMode.entity;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "custom_modes",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "user_id",
                                "mode_name"
                        }
                )
        }
)
public class CustomMode {

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, name ="mode_name")

    String modeName;

    @Column(nullable = false , name = "notification_name")
    @Enumerated(EnumType.STRING)
    NotificationMode notificationMode;

    @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "user_id",nullable = false)
      User user;

    @CreationTimestamp
            @Column(name = "created_at",nullable = false,updatable = false)
    LocalDateTime createdAt;

}
