package com.loyalty.gateway.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loyalty.gateway.repository.QRCodeRepository;
import com.loyalty.gateway.model.entity.QRCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayQRCodeConsumer {

    private final QRCodeRepository qrCodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.QR_CREATED_QUEUE)
    public void handleQrCreated(String body) {
        try {
            Map<String, Object> event = objectMapper.readValue(body, Map.class);
            String qrCode = (String) event.get("qrCode");
            String imageData = (String) event.get("qrImageData");
            log.info("Received qr.created event for {}", qrCode);

            Optional<QRCode> optional = qrCodeRepository.findByCode(qrCode);
            if (optional.isPresent()) {
                QRCode qr = optional.get();
                if (qr.getQrImageData() == null) {
                    qr.setQrImageData(imageData);
                    qrCodeRepository.save(qr);
                    log.info("Updated QRCode {} with image data", qrCode);
                } else {
                    log.info("QRCode {} already has image data, skipping", qrCode);
                }
            } else {
                log.warn("QRCode {} not found in DB", qrCode);
            }

        } catch (Exception e) {
            log.error("Failed to process qr.created event", e);
            throw new RuntimeException(e);
        }
    }
}

