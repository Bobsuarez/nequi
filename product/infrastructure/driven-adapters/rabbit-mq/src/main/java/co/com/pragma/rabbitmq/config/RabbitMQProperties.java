package co.com.pragma.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.rabbit-mq")
public record RabbitMQProperties(
        String host,
        int port,
        String username,
        String password,
        String virtualHost,
        String exchange,
        String routingKey,
        Consumer consumer
) {
    public record Consumer(String queueName) {}
}
