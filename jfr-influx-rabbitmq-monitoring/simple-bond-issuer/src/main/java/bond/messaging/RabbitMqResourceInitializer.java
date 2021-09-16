package bond.messaging;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RabbitMqResourceInitializer {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private Exchange exchange;

    @Autowired
    @Qualifier("bondQueue")
    private Queue bondQueue;

    @Autowired
    @Qualifier("bondQueueBinding")
    private Binding bondQueueBinding;

    @PostConstruct
    public void create() {

        // create broker resources if they don't exist
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(bondQueue);
        amqpAdmin.declareBinding(bondQueueBinding);
    }
}
