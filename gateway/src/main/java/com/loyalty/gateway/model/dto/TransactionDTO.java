package com.loyalty.gateway.model.dto;

import com.loyalty.gateway.model.entity.Transaction;
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
@Schema(description = "Transaction information")
public class TransactionDTO {
    
    @Schema(description = "Transaction ID")
    private Long id;

    @Schema(description = "Client email")
    private String clientEmail;

    @Schema(description = "Client name")
    private String clientName;

    @Schema(description = "Bar ID")
    private Long barId;

    @Schema(description = "Bar name")
    private String barName;

    @Schema(description = "Amount")
    private BigDecimal amount;

    @Schema(description = "Points earned/spent")
    private Integer pointsEarned;

    @Schema(description = "Transaction type")
    private String type;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Reward name (for redemptions)")
    private String rewardName;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    public static TransactionDTO fromEntity(Transaction transaction) {
        TransactionDTOBuilder builder = TransactionDTO.builder()
            .id(transaction.getId())
            .clientEmail(transaction.getClient().getEmail())
            .clientName(transaction.getClient().getFullName())
            .barId(transaction.getBar().getId())
            .barName(transaction.getBar().getName())
            .amount(transaction.getAmount())
            .pointsEarned(transaction.getPointsEarned())
            .type(transaction.getType().name())
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt());

        if (transaction.getRedemption() != null) {
            builder.rewardName(transaction.getRedemption().getReward().getName());
        }

        return builder.build();
    }
}
