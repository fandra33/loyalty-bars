package com.loyalty.gateway.controller;

import com.loyalty.gateway.model.dto.*;
import com.loyalty.gateway.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard data endpoints")
public class DashboardController {

    private final TransactionService transactionService;

    @GetMapping("/client")
    @Operation(summary = "Get client dashboard", description = "Get dashboard data for client")
    public ResponseEntity<ClientDashboard> getClientDashboard() {
        ClientDashboard dashboard = transactionService.getClientDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/bar")
    @Operation(summary = "Get bar admin dashboard", description = "Get dashboard data for bar admin")
    public ResponseEntity<BarAdminDashboard> getBarAdminDashboard() {
        BarAdminDashboard dashboard = transactionService.getBarAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
