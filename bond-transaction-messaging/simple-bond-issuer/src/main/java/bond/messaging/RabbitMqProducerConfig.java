package bond.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Profile("docker")
@Configuration
public class RabbitMqProducerConfig {

    public static final String EXCHANGE_NAME = "bond-transaction-exchange";
    public static final String QUEUE_NAME = "bond-transaction-queue";
    public static final String ROUTING_KEY = "bond.issued";

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Queue queue() {
        return QueueBuilder.nonDurable(QUEUE_NAME).autoDelete().build();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(false).autoDelete().build();
    }

    @Bean
    public Binding binding(Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }

    @PostConstruct
    public void init() {

        // create broker resources if they don't exist
        var exchange = exchange();
        amqpAdmin.declareExchange(exchange);
        var queue = queue();
        amqpAdmin.declareQueue(queue());
        amqpAdmin.declareBinding(binding(queue, exchange));
    }
}
