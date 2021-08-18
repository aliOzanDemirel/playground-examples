package transaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import transaction.domain.Transaction;
import transaction.util.Utils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransactionCache {

    private ConcurrentMap<Integer, Transaction> transactionMap;

    // counter for assigning keys to map, this also holds total transactions saved up to a point
    private final AtomicInteger transactionCounter = new AtomicInteger(0);

    @PostConstruct
    public void resetTransactions() {
        transactionMap = new ConcurrentHashMap<>();
    }

    public void saveTransaction(Transaction transaction) {
        transactionMap.putIfAbsent(transactionCounter.incrementAndGet(), transaction);
    }

    public List<BigDecimal> getTransactionsByTimeout(long validityTimeout) {

        long current = Utils.getCurrentTimeMillis();
        log.info("Will retrieve transactions that are within last {} seconds, will remove rest", validityTimeout / 1000);

        // filter out old transactions and create new map with the valid transactions
        // so that the old ones will not be consuming space in memory
        transactionMap = transactionMap.entrySet()
                .stream()
                .filter(it -> current - it.getValue().getTimestamp() <= validityTimeout)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        return transactionMap.values().stream().map(Transaction::getAmount).collect(Collectors.toList());
    }

    public int getTotalTransactionsMade() {
        return transactionCounter.get();
    }

}
