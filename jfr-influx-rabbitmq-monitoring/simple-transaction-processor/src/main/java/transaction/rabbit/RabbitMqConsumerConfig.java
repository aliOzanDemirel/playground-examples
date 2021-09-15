package transaction.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Profile("docker")
@Configuration
public class RabbitMqConsumerConfig {

    public static final String EXCHANGE_NAME = "bond-transaction-exchange";
    public static final String QUEUE_NAME = "bond-transaction-queue";
    public static final String ROUTING_KEY = "bond.issued";

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
}
