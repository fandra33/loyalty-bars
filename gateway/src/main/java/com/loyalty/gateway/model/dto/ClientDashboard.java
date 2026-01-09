package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Client dashboard")
public class ClientDashboard {
    
    @Schema(description = "Current points balance")
    private Integer pointsBalance;

    @Schema(description = "Total points earned")
    private Integer totalPointsEarned;

    @Schema(description = "Total points spent")
    private Integer totalPointsSpent;

    @Schema(description = "Total transactions")
    private Long totalTransactions;

    @Schema(description = "Recent transactions")
    private List<TransactionDTO> recentTransactions;

    @Schema(description = "Available rewards")
    private List<RewardDTO> availableRewards;
}
