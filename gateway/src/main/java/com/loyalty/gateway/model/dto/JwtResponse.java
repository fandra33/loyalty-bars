package com.loyalty.gateway.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "JWT authentication response")
public class JwtResponse {
    
    @Schema(description = "JWT access token")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String type = "Bearer";

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User role")
    private String role;

    @Schema(description = "User points balance (only for clients)")
    private Integer pointsBalance;

    public JwtResponse(String token, Long id, String email, String role, Integer pointsBalance) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.role = role;
        this.pointsBalance = pointsBalance;
    }
}
