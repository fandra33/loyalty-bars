package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByClientIdOrderByCreatedAtDesc(Long clientId);
    
    List<Transaction> findByBarIdOrderByCreatedAtDesc(Long barId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.bar.id = :barId")
    Long countByBarId(@Param("barId") Long barId);
    
    @Query("SELECT COUNT(DISTINCT t.client.id) FROM Transaction t WHERE t.bar.id = :barId")
    Long countUniqueClientsByBarId(@Param("barId") Long barId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.bar.id = :barId AND t.type = 'PURCHASE'")
    BigDecimal sumRevenueByBarId(@Param("barId") Long barId);
    
    @Query("SELECT SUM(t.pointsEarned) FROM Transaction t WHERE t.bar.id = :barId AND t.type = 'PURCHASE'")
    Integer sumPointsGivenByBarId(@Param("barId") Long barId);
    
    @Query("SELECT SUM(ABS(t.pointsEarned)) FROM Transaction t WHERE t.bar.id = :barId AND t.type = 'REDEMPTION'")
    Integer sumPointsRedeemedByBarId(@Param("barId") Long barId);
    
    @Query("SELECT SUM(t.pointsEarned) FROM Transaction t WHERE t.client.id = :clientId AND t.type = 'PURCHASE'")
    Integer sumPointsEarnedByClient(@Param("clientId") Long clientId);
    
    @Query("SELECT SUM(ABS(t.pointsEarned)) FROM Transaction t WHERE t.client.id = :clientId AND t.type = 'REDEMPTION'")
    Integer sumPointsSpentByClient(@Param("clientId") Long clientId);
}
