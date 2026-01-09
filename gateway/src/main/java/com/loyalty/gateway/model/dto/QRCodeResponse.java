package com.loyalty.gateway.model.dto;

import com.loyalty.gateway.model.entity.QRCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "QR code response")
public class QRCodeResponse {
    
    @Schema(description = "QR code string")
    private String qrCode;

    @Schema(description = "Bar name")
    private String barName;

    @Schema(description = "Amount")
    private BigDecimal amount;

    @Schema(description = "Points to be earned")
    private Integer pointsToEarn;

    @Schema(description = "Expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "QR code image data (base64)")
    private String qrImageData;

    public static QRCodeResponse fromEntity(QRCode qrCode, String qrImageData) {
        return QRCodeResponse.builder()
            .qrCode(qrCode.getCode())
            .barName(qrCode.getBar().getName())
            .amount(qrCode.getAmount())
            .pointsToEarn(qrCode.calculatePoints())
            .expiresAt(qrCode.getExpiresAt())
            .qrImageData(qrImageData)
            .build();
    }
}
