package com.aifinance.financialcompanion.admin.service;

import com.aifinance.financialcompanion.admin.dto.AdminDashboardResponse;
import com.aifinance.financialcompanion.admin.dto.AdminUserSummaryResponse;
import com.aifinance.financialcompanion.admin.dto.CreateAdminRequest;
import com.aifinance.financialcompanion.admin.dto.CreateAdminResponse;
import com.aifinance.financialcompanion.entity.User;
import com.aifinance.financialcompanion.enums.Role;
import com.aifinance.financialcompanion.exceptions.EmailAlreadyExistException;
import com.aifinance.financialcompanion.repo.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateAdminResponse createAdmin(CreateAdminRequest request){

        String email = request.email().trim().toLowerCase();

        if(userRepo.findByEmail(email).isPresent()){
            throw  new EmailAlreadyExistException("Email already exist");
        }

        User adminUser = new User();
        adminUser.setUsername(request.username());
        adminUser.setEmail(email);
        adminUser.setPassword(passwordEncoder.encode(request.password()));
        adminUser.setRole(Role.ADMIN);

        userRepo.save(adminUser);

        return new CreateAdminResponse(
                "Admin created Successfully",
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getRole().name()
        );

    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(){
        List<AdminUserSummaryResponse> users = userRepo.findAll().stream()
                .map(user -> new AdminUserSummaryResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();

        return new AdminDashboardResponse("Admin dashboard load Successfull",users);
    }
}
