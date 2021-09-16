package transaction.rabbit;

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

    // create broker resources if they don't exist

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

    @Autowired
    @Qualifier("clothingQueue")
    private Queue clothingQueue;

    @Autowired
    @Qualifier("clothingQueueBinding")
    private Binding clothingQueueBinding;

    @PostConstruct
    public void create() {

        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(bondQueue);
        amqpAdmin.declareBinding(bondQueueBinding);
        amqpAdmin.declareQueue(clothingQueue);
        amqpAdmin.declareBinding(clothingQueueBinding);
    }
}
