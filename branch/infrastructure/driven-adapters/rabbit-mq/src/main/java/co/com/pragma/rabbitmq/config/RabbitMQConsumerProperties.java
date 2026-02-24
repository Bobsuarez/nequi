package co.com.pragma.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.rabbit-mq.consumer")
public record RabbitMQConsumerProperties(
        String queueName
) {}
