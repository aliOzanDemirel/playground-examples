package clothing.service.messaging.review;

import clothing.service.domain.Review;
import clothing.service.messaging.RabbitMqProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReviewAddedTransactionProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ReviewAddedTransactionProducer(@Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Review review) {

        var event = new ReviewAddedTransactionEvent(review);
        rabbitTemplate.convertAndSend(RabbitMqProducerConfig.EXCHANGE_NAME, RabbitMqProducerConfig.CLOTHING_ROUTING_KEY, event);
        log.debug("Published transaction event for ReviewAdded: {}", event);
    }
}
