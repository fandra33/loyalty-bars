package com.loyalty.gateway.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QRCodeProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendQrRequestedEvent(String qrCode, Long barId, Long amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "qr.requested");
        event.put("qrCode", qrCode);
        event.put("barId", barId);
        event.put("amount", amount);
        rabbitTemplate.convertAndSend(RabbitConfig.QR_REQUEST_QUEUE, event);
    }
}

