package transaction.rabbit.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import transaction.domain.Transaction;
import transaction.rabbit.RabbitMqConsumerConfig;
import transaction.rabbit.model.BondIssuedEvent;
import transaction.service.TransactionService;

@Profile("docker")
@Service
@Slf4j
public class BondTransactionListener {

    private final TransactionService transactionService;

    @Autowired
    public BondTransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = RabbitMqConsumerConfig.QUEUE_NAME)
    public void messageReceived(BondIssuedEvent event) {

        log.info("Received event for issued bond: {}, creating transaction", event);
        var transaction = readTransactionFromEvent(event);
        try {
            transactionService.saveTransaction(transaction);
        } catch (Exception e) {
            log.error("Could not save transaction for bond issued event: {}", event, e);
        }
    }

    private Transaction readTransactionFromEvent(BondIssuedEvent event) {

        var transaction = new Transaction();
        transaction.setAmount(event.getAmount());
        transaction.setTimestamp(event.getTimestamp());
        return transaction;
    }
}