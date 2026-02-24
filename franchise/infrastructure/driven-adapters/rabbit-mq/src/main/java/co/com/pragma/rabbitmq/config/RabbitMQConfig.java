package co.com.pragma.rabbitmq.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

@Configuration
@ConditionalOnMissingBean(Sender.class)
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

    // Producer
    @Bean(destroyMethod = "close")
    public Sender rabbitSender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(
                new SenderOptions()
                        .connectionFactory(connectionFactory)
                        .resourceManagementScheduler(Schedulers.boundedElastic()));
    }


    // Consumer
    @Bean(destroyMethod = "close")
    public Receiver receiver(ConnectionFactory factory) {
        return RabbitFlux.createReceiver(
                new ReceiverOptions()
                        .connectionFactory(factory));
    }
}
