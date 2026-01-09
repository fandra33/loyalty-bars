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
@Schema(description = "Validate QR code request")
public class ValidateQRRequest {
    
    @NotNull(message = "QR code is required")
    @Schema(description = "QR code string", example = "QR-abc123def456")
    private String qrCode;
}
