package transaction.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import transaction.controller.mapper.TransactionRequestMapper;
import transaction.controller.request.TransactionRequest;
import transaction.service.TransactionService;

import javax.validation.Valid;

@RestController(TransactionController.TRANSACTIONS_ENDPOINT)
@Slf4j
public class TransactionController {

    public static final String TRANSACTIONS_ENDPOINT = "/transactions";
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveTransaction(@RequestBody @Valid TransactionRequest transactionBody) throws Exception {

        log.info("POST request to {} with body {}", TRANSACTIONS_ENDPOINT, transactionBody);
        var transaction = TransactionRequestMapper.convertTransactionRequestBody(transactionBody);
        transactionService.saveTransaction(transaction);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllTransactions() {

        log.info("DELETE request to {}", TRANSACTIONS_ENDPOINT);
        transactionService.deleteAllTransactions();
    }

}
