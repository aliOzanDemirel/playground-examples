package bond.messaging;

import bond.domain.Bond;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("docker")
@Service
@Slf4j
public class BondIssuedEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BondIssuedEventProducer(@Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Bond bond) {

        var event = new BondIssuedEvent(bond);
        rabbitTemplate.convertAndSend(RabbitMqProducerConfig.EXCHANGE_NAME, RabbitMqProducerConfig.ROUTING_KEY, event);
        log.info("Sent message for issued bond: {}", event);
    }
}
