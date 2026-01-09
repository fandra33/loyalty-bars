package com.loyalty.gateway.controller;

import com.loyalty.gateway.model.dto.*;
import com.loyalty.gateway.service.QRCodeService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Tag(name = "QR Codes", description = "QR code generation and validation")
public class QRController {

    private final QRCodeService qrCodeService;

    @PostMapping("/generate")
    @Operation(summary = "Generate QR code", description = "Generate a QR code for a transaction (client only)")
    public ResponseEntity<QRCodeResponse> generateQRCode(@Valid @RequestBody GenerateQRRequest request) {
        QRCodeResponse response = qrCodeService.generateQRCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate QR code", description = "Validate and process a QR code (bar admin only)")
    public ResponseEntity<TransactionDTO> validateQRCode(@Valid @RequestBody ValidateQRRequest request) {
        TransactionDTO transaction = qrCodeService.validateAndProcessQRCode(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/notification")
    @Operation(summary = "Confirm QR transaction", description = "Endpoint for QR microservice to confirm validation (internal use)")
    public ResponseEntity<Void> confirmQRTransaction(@RequestBody Map<String, Object> notification) {
        qrCodeService.handleQRNotification(notification);
        return ResponseEntity.ok().build();
    }
}
