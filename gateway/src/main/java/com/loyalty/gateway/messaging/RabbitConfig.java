package com.loyalty.gateway.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Configuration
public class RabbitConfig {

    public static final String QR_REQUEST_QUEUE = "qr.requested";
    public static final String QR_CREATED_QUEUE = "qr.created";
    public static final String QR_DLQ = "qr.dlq";
    public static final String QR_EXCHANGE = "qr.exchange";

    @Bean
    public DirectExchange qrExchange() {
        return new DirectExchange(QR_EXCHANGE, true, false);
    }

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

    @Bean
    public Binding bindingCreated(Queue qrCreatedQueue, DirectExchange qrExchange) {
        return BindingBuilder.bind(qrCreatedQueue).to(qrExchange).with(QR_CREATED_QUEUE);
    }

    @Bean
    public Binding bindingDlq(Queue qrDlq, DirectExchange qrExchange) {
        return BindingBuilder.bind(qrDlq).to(qrExchange).with(QR_DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);

        // retry template via interceptor
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .backOffOptions(1000, 2.0, 10000)
                .maxAttempts(3)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(new org.aopalliance.intercept.MethodInterceptor[]{retryInterceptor});
        factory.setErrorHandler(new RejectAndDontRequeueRecoverer());

        return factory;
    }
}
