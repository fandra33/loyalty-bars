// Locație: gateway/src/main/java/com/loyalty/gateway/service/TransactionService.java
package com.loyalty.gateway.service;

import com.loyalty.gateway.exception.CustomExceptions.*;
import com.loyalty.gateway.model.dto.*;
import com.loyalty.gateway.model.entity.Bar;
import com.loyalty.gateway.model.entity.Transaction;
import com.loyalty.gateway.model.entity.User;
import com.loyalty.gateway.model.entity.TransactionType;
import com.loyalty.gateway.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthService authService;
    private final BarService barService;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getMyTransactions() {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isClient()) {
            throw new UnauthorizedException("Only clients can view their transactions");
        }

        return transactionRepository.findByClientIdOrderByCreatedAtDesc(currentUser.getId()).stream()
            .map(TransactionDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getBarTransactions(Long barId) {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isBarAdmin()) {
            throw new UnauthorizedException("Only bar admins can view bar transactions");
        }

        barService.verifyBarOwnership(barId, currentUser);

        return transactionRepository.findByBarIdOrderByCreatedAtDesc(barId).stream()
            .map(TransactionDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getRecentTransactions(int limit) {
        User currentUser = authService.getCurrentUser();

        if (currentUser.isClient()) {
            return transactionRepository.findByClientIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .limit(limit)
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        } else if (currentUser.isBarAdmin()) {
            Bar bar = barService.getBarEntity(barService.getMyBar().getId());
            return transactionRepository.findByBarIdOrderByCreatedAtDesc(bar.getId()).stream()
                .limit(limit)
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        }

        throw new UnauthorizedException("Invalid user role");
    }

    @Transactional(readOnly = true)
    public ClientDashboard getClientDashboard() {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isClient()) {
            throw new UnauthorizedException("Only clients can access client dashboard");
        }

        Integer totalPointsEarned = transactionRepository.sumPointsEarnedByClient(currentUser.getId());
        Integer totalPointsSpent = transactionRepository.sumPointsSpentByClient(currentUser.getId());
        Long totalTransactions = (long) transactionRepository.findByClientIdOrderByCreatedAtDesc(currentUser.getId()).size();

        List<TransactionDTO> recentTransactions = transactionRepository
            .findByClientIdOrderByCreatedAtDesc(currentUser.getId()).stream()
            .limit(10)
            .map(TransactionDTO::fromEntity)
            .collect(Collectors.toList());

        return ClientDashboard.builder()
            .pointsBalance(currentUser.getPointsBalance())
            .totalPointsEarned(totalPointsEarned != null ? totalPointsEarned : 0)
            .totalPointsSpent(totalPointsSpent != null ? totalPointsSpent : 0)
            .totalTransactions(totalTransactions)
            .recentTransactions(recentTransactions)
            .build();
    }

    @Transactional(readOnly = true)
    public BarAdminDashboard getBarAdminDashboard() {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isBarAdmin()) {
            throw new UnauthorizedException("Only bar admins can access bar dashboard");
        }

        BarDTO barDTO = barService.getMyBar();
        Bar bar = barService.getBarEntity(barDTO.getId());

        Long totalCustomers = transactionRepository.countUniqueClientsByBarId(bar.getId());
        Long totalTransactions = transactionRepository.countByBarId(bar.getId());
        BigDecimal totalRevenue = transactionRepository.sumRevenueByBarId(bar.getId());
        Integer pointsGiven = transactionRepository.sumPointsGivenByBarId(bar.getId());
        Integer pointsRedeemed = transactionRepository.sumPointsRedeemedByBarId(bar.getId());

        List<TransactionDTO> recentTransactions = transactionRepository
            .findByBarIdOrderByCreatedAtDesc(bar.getId()).stream()
            .limit(10)
            .map(TransactionDTO::fromEntity)
            .collect(Collectors.toList());

        return BarAdminDashboard.builder()
            .bar(barDTO)
            .totalCustomers(totalCustomers != null ? totalCustomers : 0L)
            .totalTransactions(totalTransactions != null ? totalTransactions : 0L)
            .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .pointsGiven(pointsGiven != null ? pointsGiven : 0)
            .pointsRedeemed(pointsRedeemed != null ? pointsRedeemed : 0)
            .recentTransactions(recentTransactions)
            .build();
    }

    @Transactional
    public Transaction createTransaction(User client, Bar bar, BigDecimal amount, String description) {
        Integer points = Transaction.calculatePoints(amount);

        client.addPoints(points);

        Transaction transaction = Transaction.builder()
            .client(client)
            .bar(bar)
            .amount(amount)
            .pointsEarned(points)
            .type(Transaction.TransactionType.PURCHASE)
            .description(description)
            .build();

        transaction = transactionRepository.save(transaction);

        log.info("Transaction created: {} points for user: {} at bar: {}", 
            points, client.getEmail(), bar.getName());

        return transaction;
    }
}
