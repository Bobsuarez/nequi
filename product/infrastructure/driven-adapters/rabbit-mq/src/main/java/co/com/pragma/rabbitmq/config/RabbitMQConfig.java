package co.com.pragma.rabbitmq.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory rabbitConnectionFactory(RabbitMQProperties properties) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.host());
        factory.setPort(properties.port());
        factory.setUsername(properties.username());
        factory.setPassword(properties.password());
        factory.setVirtualHost(properties.virtualHost());
        return factory;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Sender.class)
    public Sender rabbitSender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(
                new SenderOptions()
                        .connectionFactory(connectionFactory)
                        .resourceManagementScheduler(Schedulers.boundedElastic()));
    }

    @Bean(destroyMethod = "close")
    public Receiver rabbitReceiver(ConnectionFactory connectionFactory) {
        return RabbitFlux.createReceiver(
                new ReceiverOptions().connectionFactory(connectionFactory));
    }
}
