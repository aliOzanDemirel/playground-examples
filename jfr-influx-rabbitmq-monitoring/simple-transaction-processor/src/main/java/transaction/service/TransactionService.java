package transaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import transaction.controller.response.StatisticsResponse;
import transaction.domain.Transaction;
import transaction.exception.InvalidInputException;
import transaction.exception.OldTransactionException;
import transaction.util.Utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TransactionService {

    private final TransactionCache cache;
    private final long validityTimeout;
    private final int roundingScale;

    @Autowired
    public TransactionService(TransactionCache cache,
                              @Value("${app.transaction.validity-timeout}") long validityTimeout,
                              @Value("${app.transaction.rounding-scale}") int roundingScale) {
        this.cache = cache;
        this.validityTimeout = validityTimeout;
        this.roundingScale = roundingScale;
    }

    /**
     * validate transaction's timeout and save it to cache if it is not too old compared to the configured timeout
     */
    public void saveTransaction(Transaction transaction) throws Exception {

        long transactionTimestamp = transaction.getTimestamp();
        long current = Utils.getCurrentTimeMillis();
        long diff = current - transactionTimestamp;

        if (diff < 0) {
            Utils.logAndThrowException("Transaction at " + new Date(transactionTimestamp)
                    + " is invalid, it is in the future!", InvalidInputException.class);
        } else if (diff > validityTimeout) {
            Utils.logAndThrowException("Transaction at " + new Date(transactionTimestamp) +
                    " is older than " + validityTimeout + " milliseconds", OldTransactionException.class);
        }

        cache.saveTransaction(transaction);
    }

    public void deleteAllTransactions() {
        cache.resetTransactions();
        log.info("Removed all transactions in cache, total transactions made so far: {}", cache.getTotalTransactionsMade());
    }

    /**
     * set the first transaction as minumum and maximum to be able to calculate
     * true min and max values after traversing all valid transaction amounts
     * this is necessary for the cases when there are negative amounted transactions
     */
    public StatisticsResponse getTransactionStatistics() {

        List<BigDecimal> transactionsByTimeout = cache.getTransactionsByTimeout(validityTimeout);

        StatisticsResponse resp = transactionsByTimeout.stream()
                .findFirst()
                .map(it -> {
                    StatisticsResponse starter = new StatisticsResponse();
                    starter.setMinD(it);
                    starter.setMaxD(it);
                    return transactionsByTimeout.stream().reduce(starter, this::applyAmountToStats, this::combineStats);
                })
                .orElse(new StatisticsResponse());

        return resp.finalizeStats(roundingScale);
    }

    private StatisticsResponse applyAmountToStats(StatisticsResponse statsResp, BigDecimal amount) {
        statsResp.setCount(statsResp.getCount() + 1);
        statsResp.setSumD(statsResp.getSumD().add(amount));
        if (amount.compareTo(statsResp.getMaxD()) > 0) {
            statsResp.setMaxD(amount);
        }
        if (amount.compareTo(statsResp.getMinD()) < 0) {
            statsResp.setMinD(amount);
        }
        return statsResp;
    }

    private StatisticsResponse combineStats(StatisticsResponse statsResp, StatisticsResponse combine) {
        statsResp.setCount(statsResp.getCount() + combine.getCount());
        statsResp.setSumD(statsResp.getSumD().add(combine.getSumD()));
        if (combine.getMaxD().compareTo(statsResp.getMaxD()) > 0) {
            statsResp.setMaxD(combine.getMaxD());
        }
        if (combine.getMinD().compareTo(statsResp.getMinD()) < 0) {
            statsResp.setMinD(combine.getMinD());
        }
        return statsResp;
    }

}
