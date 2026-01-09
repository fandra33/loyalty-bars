// Locație: gateway/src/main/java/com/loyalty/gateway/controller/BarController.java
package com.loyalty.gateway.controller;

import com.loyalty.gateway.model.dto.BarDTO;
import com.loyalty.gateway.service.BarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bars")
@RequiredArgsConstructor
@Tag(name = "Bars", description = "Bar management endpoints")
public class BarController {

    private final BarService barService;

    @GetMapping
    @Operation(summary = "Get all active bars", description = "List all active partner bars")
    public ResponseEntity<List<BarDTO>> getAllBars() {
        List<BarDTO> bars = barService.getAllActiveBars();
        return ResponseEntity.ok(bars);
    }

    @GetMapping("/{barId}")
    @Operation(summary = "Get bar by ID", description = "Get details of a specific bar")
    public ResponseEntity<BarDTO> getBarById(
            @Parameter(description = "Bar ID") @PathVariable Long barId) {
        BarDTO bar = barService.getBarById(barId);
        return ResponseEntity.ok(bar);
    }

    @GetMapping("/my-bar")
    @Operation(summary = "Get my bar", description = "Get bar owned by current bar admin")
    public ResponseEntity<BarDTO> getMyBar() {
        BarDTO bar = barService.getMyBar();
        return ResponseEntity.ok(bar);
    }

    @GetMapping("/search")
    @Operation(summary = "Search bars", description = "Search bars by name or address")
    public ResponseEntity<List<BarDTO>> searchBars(
            @Parameter(description = "Search term") @RequestParam(required = false) String q) {
        List<BarDTO> bars = barService.searchBars(q);
        return ResponseEntity.ok(bars);
    }
}
