package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    
    List<Redemption> findByRewardId(Long rewardId);
    
    @Query("SELECT r FROM Redemption r WHERE r.transaction.client.id = :clientId ORDER BY r.createdAt DESC")
    List<Redemption> findByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT COUNT(r) FROM Redemption r WHERE r.reward.id = :rewardId")
    Long countByRewardId(@Param("rewardId") Long rewardId);
}
