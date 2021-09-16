package transaction.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import transaction.controller.response.StatisticsResponse;
import transaction.service.TransactionService;

@RestController
@Slf4j
public class StatisticsController {

    public static final String STATISTICS_ENDPOINT = "/statistics";
    private final TransactionService transactionService;

    @Autowired
    public StatisticsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(StatisticsController.STATISTICS_ENDPOINT)
    public StatisticsResponse getStatistics() {

        log.info("GET request to {}", STATISTICS_ENDPOINT);
        return transactionService.getTransactionStatistics();
    }

}
