package com.loyalty.gateway.model.dto;

import com.loyalty.gateway.model.entity.Bar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bar information")
public class BarDTO {
    
    @Schema(description = "Bar ID")
    private Long id;

    @Schema(description = "Bar name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Phone")
    private String phone;

    @Schema(description = "Active status")
    private Boolean active;

    @Schema(description = "Owner email")
    private String ownerEmail;

    public static BarDTO fromEntity(Bar bar) {
        return BarDTO.builder()
            .id(bar.getId())
            .name(bar.getName())
            .description(bar.getDescription())
            .address(bar.getAddress())
            .phone(bar.getPhone())
            .active(bar.getActive())
            .ownerEmail(bar.getOwner().getEmail())
            .build();
    }
}
