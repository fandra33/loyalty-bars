package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bar admin dashboard")
public class BarAdminDashboard {
    
    @Schema(description = "Bar information")
    private BarDTO bar;

    @Schema(description = "Total customers")
    private Long totalCustomers;

    @Schema(description = "Total transactions")
    private Long totalTransactions;

    @Schema(description = "Total revenue")
    private BigDecimal totalRevenue;

    @Schema(description = "Points given")
    private Integer pointsGiven;

    @Schema(description = "Points redeemed")
    private Integer pointsRedeemed;

    @Schema(description = "Recent transactions")
    private List<TransactionDTO> recentTransactions;
}
