package com.loyalty.gateway.controller;

import com.loyalty.gateway.model.dto.*;
import com.loyalty.gateway.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards", description = "Reward management endpoints")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    @Operation(summary = "Get all rewards", description = "List all available rewards")
    public ResponseEntity<List<RewardDTO>> getAllRewards() {
        List<RewardDTO> rewards = rewardService.getAllRewards();
        return ResponseEntity.ok(rewards);
    }

    @GetMapping("/{rewardId}")
    @Operation(summary = "Get reward by ID", description = "Get details of a specific reward")
    public ResponseEntity<RewardDTO> getRewardById(
            @Parameter(description = "Reward ID") @PathVariable Long rewardId) {
        RewardDTO reward = rewardService.getRewardById(rewardId);
        return ResponseEntity.ok(reward);
    }

    @GetMapping("/bar/{barId}")
    @Operation(summary = "Get rewards by bar", description = "List all rewards for a specific bar")
    public ResponseEntity<List<RewardDTO>> getRewardsByBar(
            @Parameter(description = "Bar ID") @PathVariable Long barId) {
        List<RewardDTO> rewards = rewardService.getRewardsByBar(barId);
        return ResponseEntity.ok(rewards);
    }

    @GetMapping("/affordable")
    @Operation(summary = "Get affordable rewards", description = "List rewards that current user can afford")
    public ResponseEntity<List<RewardDTO>> getAffordableRewards() {
        List<RewardDTO> rewards = rewardService.getAffordableRewards();
        return ResponseEntity.ok(rewards);
    }

    @PostMapping("/create")
    @Operation(summary = "Create reward", description = "Create a new reward (bar admin only)")
    public ResponseEntity<RewardDTO> createReward(@Valid @RequestBody CreateRewardRequest request) {
        RewardDTO reward = rewardService.createReward(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reward);
    }

    @PostMapping("/redeem")
    @Operation(summary = "Redeem reward", description = "Redeem a reward with points (client only)")
    public ResponseEntity<TransactionDTO> redeemReward(@Valid @RequestBody RedeemRewardRequest request) {
        TransactionDTO transaction = rewardService.redeemReward(request);
        return ResponseEntity.ok(transaction);
    }
}
