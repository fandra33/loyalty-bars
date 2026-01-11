package com.loyalty.gateway.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QR_REQUEST_QUEUE = "qr.requested";
    public static final String QR_CREATED_QUEUE = "qr.created";
    public static final String QR_DLQ = "qr.dlq";

    @Bean
    public Queue qrRequestQueue() {
        return new Queue(QR_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue qrCreatedQueue() {
        return new Queue(QR_CREATED_QUEUE, true);
    }

    @Bean
    public Queue qrDlq() {
        return new Queue(QR_DLQ, true);
    }
}
