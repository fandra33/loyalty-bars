package com.loyalty.gateway.service;

import com.loyalty.gateway.exception.CustomExceptions.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRServiceClient {

    @Value("${qr-service.url}")
    private String qrServiceUrl;

    private final WebClient.Builder webClientBuilder;

    public QRGenerationResponse generateQRCode(String code, Long barId, BigDecimal amount) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(qrServiceUrl).build();

            QRGenerationResponse response = webClient.post()
                    .uri("/api/qr/generate")
                    .bodyValue(Map.of(
                            "code", code,
                            "bar_id", barId,
                            "amount", amount.toString()))
                    .retrieve()
                    .bodyToMono(QRGenerationResponse.class)
                    .block();

            if (response == null) {
                throw new ServiceException("Failed to generate QR code: null response");
            }

            log.info("QR code generated successfully for code: {}", code);
            return response;

        } catch (Exception e) {
            log.error("Error calling QR service for code generation: {}", e.getMessage());
            throw new ServiceException("QR service is unavailable: " + e.getMessage());
        }
    }

    public QRValidationResponse validateQRCode(String code) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(qrServiceUrl).build();

            QRValidationResponse response = webClient.post()
                    .uri("/api/qr/validate")
                    .bodyValue(Map.of("code", code))
                    .retrieve()
                    .bodyToMono(QRValidationResponse.class)
                    .block();

            if (response == null) {
                throw new ServiceException("Failed to validate QR code: null response");
            }

            log.info("QR code validation result for {}: {}", code, response.isValid());
            return response;

        } catch (Exception e) {
            log.error("Error calling QR service for validation: {}", e.getMessage());
            throw new ServiceException("QR service is unavailable: " + e.getMessage());
        }
    }

    public boolean isQRServiceHealthy() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(qrServiceUrl).build();

            Map<String, Object> healthResponse = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return healthResponse != null && "healthy".equals(healthResponse.get("status"));

        } catch (Exception e) {
            log.warn("QR service health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    public static class QRGenerationResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("qr_image_data")
        private String qrImageData;
        private String message;
        private boolean success;
    }

    @Data
    @NoArgsConstructor
    public static class QRValidationResponse {
        private boolean valid;
        private String message;
        private String reason;
    }
}
