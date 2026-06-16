package com.aifinance.financialcompanion.notification.entity;

import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.NotificationSeverity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notification_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "message",nullable = false,length = 500)
    private String message;

   @Enumerated(EnumType.STRING)
   @Column(name = "severity",nullable = false)
   private NotificationSeverity severity;

   @CreationTimestamp
   @Column(name = "sent_at",nullable = false,updatable = false)
   private LocalDateTime sentAt;

    public NotificationLog(User user, String message, NotificationSeverity severity, LocalDateTime sentAt) {
        this.user = user;
        this.message = message;
        this.severity = severity;
        this.sentAt = sentAt;
    }
}
