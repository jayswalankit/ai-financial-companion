package com.aifinance.financialcompanion.entity;


import com.aifinance.financialcompanion.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Builder
@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY )
         private  Long id;

        @Column(nullable = false)
    private String username;

        @Column(nullable = false,unique = true)
      private String email;

        @Column(nullable = false)
        private String password;

        @Enumerated(EnumType.STRING)  // tell us that enums type jo hai so String form me hai warna 0,1 me form me hota
        @Column(nullable = false)
          private Role role;

        @Column(nullable = false)
        @Builder.Default
    private boolean emailVerified = false;

}
