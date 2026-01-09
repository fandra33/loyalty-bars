package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Redeem reward request")
public class RedeemRewardRequest {
    
    @NotNull(message = "Reward ID is required")
    @Schema(description = "Reward ID", example = "1")
    private Long rewardId;
}
