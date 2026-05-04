package com.aifinance.financialcompanion.admin.controller;

import com.aifinance.financialcompanion.admin.dto.AdminDashboardResponse;
import com.aifinance.financialcompanion.admin.dto.CreateAdminRequest;
import com.aifinance.financialcompanion.admin.dto.CreateAdminResponse;
import com.aifinance.financialcompanion.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        AdminDashboardResponse response = adminService.getDashboard();
        log.info("Admin dashboard accessed");
        return new ResponseEntity<>(response, HttpStatus.OK );

    }

    @PostMapping("/users")
    public ResponseEntity<CreateAdminResponse>createAdmin(@RequestBody @Valid CreateAdminRequest request){

     CreateAdminResponse response = adminService.createAdmin(request);
     log.info("New admin registered");
        return new ResponseEntity<>(response ,HttpStatus.CREATED);

    }
}
