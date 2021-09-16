package bond.messaging.event;

import bond.domain.Bond;
import bond.messaging.RabbitMqProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BondIssuedTransactionProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BondIssuedTransactionProducer(@Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Bond bond) {

        var event = new BondIssuedTransactionEvent(bond);
        rabbitTemplate.convertAndSend(RabbitMqProducerConfig.EXCHANGE_NAME, RabbitMqProducerConfig.BOND_ROUTING_KEY, event);
        log.debug("Published transaction event for BondIssued: {}", event);
    }
}
