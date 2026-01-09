package com.loyalty.gateway.model.dto;

import com.loyalty.gateway.model.entity.Reward;
import com.loyalty.gateway.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Reward information")
public class RewardDTO {
    
    @Schema(description = "Reward ID")
    private Long id;

    @Schema(description = "Bar ID")
    private Long barId;

    @Schema(description = "Bar name")
    private String barName;

    @Schema(description = "Reward name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Points cost")
    private Integer pointsCost;

    @Schema(description = "Active status")
    private Boolean active;

    @Schema(description = "Can be redeemed by current user")
    private Boolean canRedeem;

    public static RewardDTO fromEntity(Reward reward, User currentUser) {
        return RewardDTO.builder()
            .id(reward.getId())
            .barId(reward.getBar().getId())
            .barName(reward.getBar().getName())
            .name(reward.getName())
            .description(reward.getDescription())
            .pointsCost(reward.getPointsCost())
            .active(reward.getActive())
            .canRedeem(currentUser != null && reward.canBeRedeemedBy(currentUser))
            .build();
    }
}
