package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generate QR code request")
public class GenerateQRRequest {
    
    @NotNull(message = "Bar ID is required")
    @Schema(description = "Bar ID", example = "1")
    private Long barId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Transaction amount", example = "50.00")
    private BigDecimal amount;
}
