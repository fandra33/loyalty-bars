// Locație: gateway/src/main/java/com/loyalty/gateway/service/RewardService.java
package com.loyalty.gateway.service;

import com.loyalty.gateway.exception.CustomExceptions.*;
import com.loyalty.gateway.model.dto.*;
import com.loyalty.gateway.model.entity.*;
import com.loyalty.gateway.repository.RedemptionRepository;
import com.loyalty.gateway.repository.RewardRepository;
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
public class RewardService {

    private final RewardRepository rewardRepository;
    private final TransactionRepository transactionRepository;
    private final RedemptionRepository redemptionRepository;
    private final BarService barService;
    private final AuthService authService;
    private final WebSocketService webSocketService;

    @Transactional(readOnly = true)
    public List<RewardDTO> getAllRewards() {
        User currentUser = authService.getCurrentUser();
        
        return rewardRepository.findAll().stream()
            .filter(Reward::getActive)
            .map(reward -> RewardDTO.fromEntity(reward, currentUser))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RewardDTO> getRewardsByBar(Long barId) {
        User currentUser = authService.getCurrentUser();
        
        return rewardRepository.findByBarIdAndActiveTrue(barId).stream()
            .map(reward -> RewardDTO.fromEntity(reward, currentUser))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RewardDTO> getAffordableRewards() {
        User currentUser = authService.getCurrentUser();
        
        if (!currentUser.isClient()) {
            throw new UnauthorizedException("Only clients can view affordable rewards");
        }

        return rewardRepository.findAffordableRewards(currentUser.getPointsBalance()).stream()
            .map(reward -> RewardDTO.fromEntity(reward, currentUser))
            .collect(Collectors.toList());
    }

    @Transactional
    public RewardDTO createReward(CreateRewardRequest request) {
        User currentUser = authService.getCurrentUser();
        
        if (!currentUser.isBarAdmin()) {
            throw new UnauthorizedException("Only bar admins can create rewards");
        }

        barService.verifyBarOwnership(request.getBarId(), currentUser);
        Bar bar = barService.getBarEntity(request.getBarId());

        Reward reward = Reward.builder()
            .bar(bar)
            .name(request.getName())
            .description(request.getDescription())
            .pointsCost(request.getPointsCost())
            .active(true)
            .build();

        reward = rewardRepository.save(reward);
        log.info("Reward created: {} for bar: {}", reward.getName(), bar.getName());

        return RewardDTO.fromEntity(reward, currentUser);
    }

    @Transactional
    public TransactionDTO redeemReward(RedeemRewardRequest request) {
        User currentUser = authService.getCurrentUser();

        if (!currentUser.isClient()) {
            throw new UnauthorizedException("Only clients can redeem rewards");
        }

        Reward reward = rewardRepository.findById(request.getRewardId())
            .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        if (!reward.getActive()) {
            throw new BadRequestException("Reward is not active");
        }

        if (!reward.canBeRedeemedBy(currentUser)) {
            throw new BadRequestException("Insufficient points to redeem this reward");
        }

        currentUser.deductPoints(reward.getPointsCost());

        Transaction transaction = Transaction.builder()
            .client(currentUser)
            .bar(reward.getBar())
            .amount(BigDecimal.ZERO)
            .pointsEarned(-reward.getPointsCost())
            .type(Transaction.TransactionType.REDEMPTION)
            .description("Redeemed: " + reward.getName())
            .build();

        transaction = transactionRepository.save(transaction);

        Redemption redemption = Redemption.builder()
            .transaction(transaction)
            .reward(reward)
            .build();

        redemptionRepository.save(redemption);

        log.info("Reward redeemed: {} by user: {}", reward.getName(), currentUser.getEmail());

        webSocketService.notifyPointsUpdate(currentUser, "REWARD_REDEEMED", reward.getPointsCost());

        return TransactionDTO.fromEntity(transaction);
    }

    @Transactional(readOnly = true)
    public RewardDTO getRewardById(Long rewardId) {
        User currentUser = authService.getCurrentUser();
        
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        return RewardDTO.fromEntity(reward, currentUser);
    }
}
