package transaction.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitMqConsumerConfig {

    public static final String EXCHANGE_NAME = "transaction-exchange";
    public static final String BOND_QUEUE_NAME = "bond-transaction-queue";
    public static final String BOND_ROUTING_KEY = "bond.issued";
    public static final String CLOTHING_QUEUE_NAME = "clothing-transaction-queue";
    public static final String CLOTHING_ROUTING_KEY = "review.added";

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {

        // configure json deserialization
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(false).autoDelete().build();
    }

    @Bean
    public Queue bondQueue() {
        return QueueBuilder.nonDurable(BOND_QUEUE_NAME).autoDelete().build();
    }

    @Bean
    public Binding bondQueueBinding(@Qualifier("bondQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BOND_ROUTING_KEY).noargs();
    }

    @Bean
    public Queue clothingQueue() {
        return QueueBuilder.nonDurable(CLOTHING_QUEUE_NAME).autoDelete().build();
    }

    @Bean
    public Binding clothingQueueBinding(@Qualifier("clothingQueue") Queue queue, Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(CLOTHING_ROUTING_KEY).noargs();
    }
}
