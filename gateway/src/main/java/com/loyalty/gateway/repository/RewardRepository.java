package com.loyalty.gateway.repository;

import com.loyalty.gateway.model.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    List<Reward> findByBarId(Long barId);
    
    List<Reward> findByBarIdAndActiveTrue(Long barId);
    
    @Query("SELECT r FROM Reward r WHERE r.active = true AND r.pointsCost <= :maxPoints")
    List<Reward> findAffordableRewards(@Param("maxPoints") Integer maxPoints);
    
    @Query("SELECT r FROM Reward r WHERE r.bar.id = :barId AND r.active = true AND r.pointsCost <= :maxPoints")
    List<Reward> findAffordableRewardsByBar(@Param("barId") Long barId, @Param("maxPoints") Integer maxPoints);
}
