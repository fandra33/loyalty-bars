package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create reward request")
public class CreateRewardRequest {
    
    @NotNull(message = "Bar ID is required")
    @Schema(description = "Bar ID", example = "1")
    private Long barId;

    @NotNull(message = "Name is required")
    @Schema(description = "Reward name", example = "Free Coffee")
    private String name;

    @Schema(description = "Description", example = "Get a free coffee of your choice")
    private String description;

    @NotNull(message = "Points cost is required")
    @Positive(message = "Points cost must be positive")
    @Schema(description = "Points required", example = "10")
    private Integer pointsCost;
}
