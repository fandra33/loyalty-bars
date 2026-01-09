package com.loyalty.gateway.model.dto;

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
@Schema(description = "User profile")
public class UserProfile {
    
    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Role")
    private String role;

    @Schema(description = "Points balance")
    private Integer pointsBalance;

    @Schema(description = "Account active status")
    private Boolean active;

    public static UserProfile fromEntity(User user) {
        return UserProfile.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole().name())
            .pointsBalance(user.getPointsBalance())
            .active(user.getActive())
            .build();
    }
}
