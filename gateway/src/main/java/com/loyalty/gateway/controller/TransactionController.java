package com.loyalty.gateway.controller;

import com.loyalty.gateway.model.dto.TransactionDTO;
import com.loyalty.gateway.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction history endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/my-transactions")
    @Operation(summary = "Get my transactions", description = "Get transaction history for current user (client only)")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions() {
        List<TransactionDTO> transactions = transactionService.getMyTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/bar/{barId}")
    @Operation(summary = "Get bar transactions", description = "Get transactions for a bar (bar admin only)")
    public ResponseEntity<List<TransactionDTO>> getBarTransactions(
            @Parameter(description = "Bar ID") @PathVariable Long barId) {
        List<TransactionDTO> transactions = transactionService.getBarTransactions(barId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions", description = "Get recent transactions (last N)")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<TransactionDTO> transactions = transactionService.getRecentTransactions(limit);
        return ResponseEntity.ok(transactions);
    }
}
