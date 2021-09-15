package transaction.rabbit;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("docker")
@Component
public class RabbitMqResourceInitializer {

    // create broker resources if they don't exist

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private Exchange exchange;

    @Autowired
    private Queue queue;

    @Autowired
    private Binding binding;

    @PostConstruct
    public void create() {

        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
    }
}
