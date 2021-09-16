package transaction.rabbit.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import transaction.domain.Transaction;
import transaction.domain.TransactionSource;
import transaction.rabbit.RabbitMqConsumerConfig;
import transaction.rabbit.model.ReviewAddedEvent;
import transaction.service.TransactionService;

import java.math.BigDecimal;

@Service
@Slf4j
public class ClothingTransactionListener {

    private final TransactionService transactionService;

    @Autowired
    public ClothingTransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = RabbitMqConsumerConfig.CLOTHING_QUEUE_NAME)
    public void messageReceived(ReviewAddedEvent event) {

        log.info("Received message for added review to clothing: {}, saving transaction", event);

        var transaction = readTransactionFromEvent(event);
        try {
            transactionService.saveTransaction(transaction);
        } catch (Exception e) {
            log.error("Could not save transaction for added review event: {}", event, e);
        }
    }

    private Transaction readTransactionFromEvent(ReviewAddedEvent event) {

        var transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(event.getRating()));
        transaction.setTimestamp(event.getTimestamp());
        transaction.setSourceSystem(TransactionSource.CLOTHING_SERVICE);
        return transaction;
    }
}